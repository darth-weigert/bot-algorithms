package br.darthweigert.algorithm

import korlibs.image.color.Colors
import korlibs.korge.ui.UIText
import korlibs.korge.view.Container
import korlibs.korge.view.circle
import korlibs.korge.view.line
import korlibs.math.geom.Point
import kotlinx.coroutines.delay
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

class DebugFunnel(
    private val status: UIText,
    private val debugContainer: Container,
    private val stepDelay: Duration = 1.seconds,
    var speedUp: Double = 1.0
) : Funnel.Debug {

    private val points = mutableListOf<Point>()

    fun reset() {
        speedUp = 1.0
        points.clear()
    }

    override suspend fun portal(left: Point, right: Point) {
        status.text = "Portal: $left, $right"
        debugContainer.line(left, right, color = Colors.YELLOW)
        debugContainer.circle(6, fill = Colors.ORANGERED) {
            x = left.x - 6.0
            y = left.y - 6.0
        }
        debugContainer.circle(6, fill = Colors.SEAGREEN) {
            x = right.x - 6.0
            y = right.y - 6.0
        }
        delay(stepDelay / speedUp)
    }

    override suspend fun currentFunnel(portalApex: Point, portalLeft: Point, portalRight: Point) {
        status.text = "Current: apex$portalApex, left$portalLeft, right$portalRight"
        debugContainer.removeChildren()
        drawCurrentPath()
        debugContainer.circle(6, fill = Colors.GREENYELLOW) {
            x = portalApex.x - 6.0
            y = portalApex.y - 6.0
        }
        debugContainer.line(portalApex, portalLeft, color = Colors.ORANGERED)
        debugContainer.line(portalApex, portalRight, color = Colors.SEAGREEN)
        delay(stepDelay / speedUp)
    }

    override suspend fun point(point: Point) {
        status.text = "Add point $point to result"
        points.add(point)
        debugContainer.circle(6, fill = Colors.GREENYELLOW) {
            x = point.x - 6.0
            y = point.y - 6.0
        }
        delay(stepDelay / speedUp)
    }

    override suspend fun funnelCrossed(
        portalApex: Point,
        leftChange: Pair<Point, Point>,
        rightChange: Pair<Point, Point>
    ) {
        for (index in 0..4) {
            debugContainer.removeChildren()
            drawCurrentPath()
            debugContainer.line(portalApex, leftChange.first, Colors.ORANGERED)
            debugContainer.line(portalApex, rightChange.first, Colors.SEAGREEN)
            delay(stepDelay / 4 / speedUp)
            debugContainer.removeChildren()
            drawCurrentPath()
            debugContainer.line(portalApex, leftChange.second, Colors.ORANGERED)
            debugContainer.line(portalApex, rightChange.second, Colors.SEAGREEN)
            delay(stepDelay / 4 / speedUp)
        }
    }

    private fun drawCurrentPath() {
        for (point in points) {
            debugContainer.circle(6, fill = Colors.GREENYELLOW) {
                x = point.x - 6.0
                y = point.y - 6.0
            }
        }
        points.zipWithNext().forEach { (a, b) -> debugContainer.line(a, b, Colors.WHITE) }
    }
}
