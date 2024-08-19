package br.darthweigert.algorithm

import br.darthweigert.geometry.Triangle
import korlibs.image.color.Colors
import korlibs.korge.ui.UIText
import korlibs.korge.view.Container
import korlibs.korge.view.SolidTriangle
import korlibs.korge.view.Text
import korlibs.korge.view.line
import korlibs.korge.view.solidTriangle
import korlibs.korge.view.text
import korlibs.math.geom.Line2D
import kotlinx.coroutines.delay
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

class DebugAStar(
    private val status: UIText,
    private val debugContainer: Container,
    private val stepDelay: Duration = 1.seconds,
    var speedUp: Double = 1.0
) : AStar.Debug<Triangle, Line2D> {

    private val frontier = mutableMapOf<Triangle, SolidTriangle>()
    private var current: SolidTriangle? = null
    private val values = mutableMapOf<Triangle, Text>()

    fun reset() {
        speedUp = 1.0
        frontier.clear()
        current = null
        values.clear()
    }

    override suspend fun dequeue(state: Triangle) {
        status.text = "Dequeue: $state"
        debugContainer.removeChildren()
        frontier.remove(state)
        frontier.values.forEach(debugContainer::addChild)
        current = debugContainer.solidTriangle(state.a, state.b, state.c, Colors.GREENYELLOW)
        values.values.forEach(debugContainer::addChild)
        values.remove(state)
        delay(stepDelay / speedUp)
    }

    override suspend fun expansion(expandedStates: List<Pair<Line2D, Triangle>>) {
        status.text = "Expanded ${expandedStates.size} nodes"
        for ((edge, triangle) in expandedStates) {
            frontier[triangle] = debugContainer.solidTriangle(triangle.a, triangle.b, triangle.c, Colors.BLUEVIOLET)
            debugContainer.line(edge.a, edge.b, Colors.LIMEGREEN)
        }
        delay(stepDelay / speedUp)
    }

    override suspend fun calculatedValues(state: Triangle, cost: Int, heuristic: Int) {
        status.text = "Values for $state is: cost = $cost, heuristic = $heuristic"
        values[state] = debugContainer.text("[$cost,$heuristic]") {
            val centroid = state.centroid
            x = centroid.x - width / 2
            y = centroid.y - height / 2
        }
        delay(stepDelay / speedUp)
    }

    override suspend fun shortCut(
        oldFromState: Triangle,
        oldToState: Triangle,
        newFromState: Triangle,
        newToState: Triangle
    ) {
        status.text = "Found a short-cut!"
        for (loop in 0..6) {
            debugContainer.removeChildren()
            delay(stepDelay / 4 / speedUp)
            debugContainer.solidTriangle(oldFromState.a, oldFromState.b, oldFromState.c, Colors.DARKRED)
            debugContainer.solidTriangle(oldToState.a, oldToState.b, oldToState.c, Colors.BLUEVIOLET)
            delay(stepDelay / 4 / speedUp)
        }
        debugContainer.removeChildren()
        debugContainer.solidTriangle(newFromState.a, newFromState.b, newFromState.c, Colors.GREENYELLOW)
        debugContainer.solidTriangle(newToState.a, newToState.b, newToState.c, Colors.BLUEVIOLET)
        delay(stepDelay / speedUp)
    }
}
