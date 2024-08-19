package br.darthweigert.algorithm

import br.darthweigert.geometry.Polygon
import br.darthweigert.geometry.Triangle
import br.darthweigert.geometry.toVectorPath
import br.darthweigert.math.nextRandomGray
import korlibs.image.color.Colors
import korlibs.image.color.RGBA
import korlibs.korge.ui.UIText
import korlibs.korge.view.Container
import korlibs.korge.view.line
import korlibs.korge.view.outline
import korlibs.korge.view.solidTriangle
import korlibs.math.geom.Point
import kotlinx.coroutines.delay
import kotlin.random.Random
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

class DebugTriangulation(
    private val status: UIText,
    private val debugContainer: Container,
    private val stepDelay: Duration = 1.seconds,
    private val startStepDelay: Duration = stepDelay,
    private val bridgeHoleDelay: Duration = stepDelay,
    private val intermediatePolygonDelay: Duration = stepDelay,
    private val earCandidateDelay: Duration = stepDelay,
    private val earToClipDelay: Duration = stepDelay,
    var speedUp: Double = 1.0
): Triangulation.Debug {

    private lateinit var originalPolygon: Polygon
    private lateinit var polygon: MutableList<Point>
    private lateinit var polygonIndices: MutableList<Int>
    private val candidates = mutableListOf<Triangle>()
    private val triangles = mutableListOf<Pair<Triangle, RGBA>>()
    private val random = Random(1234)

    fun reset() {
        speedUp = 1.0
        candidates.clear()
        triangles.clear()
    }

    override suspend fun start(polygon: Polygon) {
        originalPolygon = polygon
        debugContainer.removeChildren()
        debugContainer.outline(originalPolygon.points.toVectorPath(), color = Colors.WHITE)
        for (hole in originalPolygon.holes) {
            debugContainer.outline(hole.points.toVectorPath(), color = Colors.WHITE)
        }
        delay(startStepDelay / speedUp)
    }

    override suspend fun bridgeHole(borderPoint: Point, holePoint: Point) {
        status.text = "Bridge hole from $borderPoint to $holePoint"
        debugContainer.line(borderPoint, holePoint)
        delay(bridgeHoleDelay / speedUp)
    }

    override suspend fun intermediatePolygon(points: List<Point>) {
        status.text = "Polygon without holes: $points"
        debugContainer.removeChildren()
        debugContainer.outline(points.toVectorPath())
        polygon = points.toMutableList()
        polygonIndices = polygon.indices.toMutableList()
        delay(intermediatePolygonDelay / speedUp)
    }

    override suspend fun earCandidate(index: Int, ear: Triangle) {
        candidates.add(ear)
        debugContainer.removeChildren()
        debugContainer.outline(polygon.toVectorPath())
        for (triangle in candidates) {
            debugContainer.solidTriangle(triangle.a, triangle.b, triangle.c, Colors.BLUEVIOLET)
        }
        for ((triangle, color) in triangles) {
            debugContainer.solidTriangle(triangle.a, triangle.b, triangle.c, color)
        }
        delay(earCandidateDelay / speedUp)
    }

    override suspend fun earToClip(index: Int, ear: Triangle) {
        candidates.clear()
        debugContainer.solidTriangle(ear.a, ear.b, ear.c, Colors.GREENYELLOW)
        val position = polygonIndices.indexOf(index)
        polygonIndices.remove(index)
        polygon.removeAt(position)
        triangles.add(ear to random.nextRandomGray().withA(127))
        delay(earToClipDelay / speedUp)
    }
}
