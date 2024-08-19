package br.darthweigert.algorithm

import br.darthweigert.geometry.Triangle
import korlibs.math.geom.Line2D

class NavMeshPathFinder(navMesh: List<Triangle>) : AStar.Model<Triangle, Line2D, Triangle> {
    private val graph = Graph.UndirectedGraph<Triangle, Unit?>()

    init {
        for (triangle in navMesh) {
            graph.add(triangle)
        }
        for (triangleA in navMesh) {
            var foundNeighbors = 0
            for (triangleB in navMesh) {
                val containsA = triangleA.a in triangleB.points
                val containsB = triangleA.b in triangleB.points
                val containsC = triangleA.c in triangleB.points
                if ((containsA && containsB && !containsC) || (containsA && !containsB && containsC) || (!containsA && containsB && containsC)) {
                    graph.add(triangleA, triangleB, null)
                    if (++foundNeighbors == 3) {
                        break
                    }
                }
            }
        }
    }

    override fun reachGoal(state: Triangle, goal: Triangle): Boolean {
        return state == goal
    }

    override fun expand(state: Triangle): Sequence<Pair<Line2D, Triangle>> {
        return graph.getLinks(state)!!.asSequence()
            .map { neighbor ->
                val edge = neighbor.sharedEdge(state)
                edge to neighbor
            }
    }

    override fun calculateHeuristic(state: Triangle, goal: Triangle): Int {
        return (goal.centroid - state.centroid).length.toInt()
    }

    override fun calculateCost(parent: Triangle, state: Triangle, edge: Line2D): Int {
        return (state.centroid - parent.centroid).length.toInt()
    }
}
