package br.darthweigert.algorithm

import korlibs.datastructure.PriorityQueue

class AStar<S, E, G>(
    private val model: Model<S, E, G>,
    @Suppress("UNCHECKED_CAST") private val debug: Debug<S, E> = Debug.NO_OP as Debug<S, E>
) {
    suspend fun find(start: S, goal: G): List<Pair<E, S>> {
        val queue = PriorityQueue<Node<S, E>>()
        val explored = mutableSetOf<S>()
        val frontier = mutableSetOf<S>()

        queue.add(Node(0, start, null, 0, model.calculateHeuristic(start, goal)))

        while(queue.isNotEmpty()) {
            val node = queue.removeHead()
            debug.dequeue(node.state)
            if (model.reachGoal(node.state, goal)) {
                val path = mutableListOf<Pair<E, S>>()
                var step: Node<S, E> = node
                while (step.edge != null) {
                    path.add(0, step.edge!! to step.state)
                    step = step.parent!!
                }
                return path
            }
            frontier.remove(node.state)
            explored.add(node.state)
            val expansion = model.expand(node.state).filter { (_, connected) ->
                connected !in explored
            }.toList()
            debug.expansion(expansion)
            for ((edge, connected) in expansion) {
                val childNode = Node(
                    node.depth + 1,
                    connected,
                    edge,
                    node.cost + model.calculateCost(node.state, connected, edge),
                    model.calculateHeuristic(connected, goal),
                    node)
                debug.calculatedValues(connected, childNode.cost, childNode.heuristic)
                if (frontier.add(connected)) {
                    queue.add(childNode)
                } else {
                    val existing = queue.find { it.state == connected }!!
                    if (existing.sum > childNode.sum) {
                        queue.remove(existing)
                        queue.add(childNode)
                        debug.shortCut(existing.parent!!.state, existing.state, node.state, childNode.state)
                    }
                }
            }
        }

        return listOf()
    }

    class Node<S, E>(
        val depth: Int,
        val state: S,
        val edge: E?,
        val cost: Int,
        val heuristic: Int,
        val parent: Node<S, E>? = null
    ): Comparable<Node<S, E>> {
        val sum = heuristic + cost
        override fun compareTo(other: Node<S, E>): Int {
            return sum.compareTo(other.sum)
        }
    }

    interface Model<S, E, G> {
        fun reachGoal(state: S, goal: G): Boolean
        fun expand(state: S): Sequence<Pair<E, S>>
        fun calculateHeuristic(state: S, goal: G): Int
        fun calculateCost(parent: S, state: S, edge: E): Int
    }

    interface Debug<S, E> {
        companion object {
            val NO_OP: Debug<Any, Any> = object : Debug<Any, Any> {
                override suspend fun dequeue(state: Any) { }
                override suspend fun expansion(expandedStates: List<Pair<Any, Any>>) { }
                override suspend fun calculatedValues(state: Any, cost: Int, heuristic: Int) { }
                override suspend fun shortCut(oldFromState: Any, oldToState: Any, newFromState: Any, newToState: Any) { }
            }
        }

        suspend fun dequeue(state: S)
        suspend fun expansion(expandedStates: List<Pair<E, S>>)
        suspend fun calculatedValues(state: S, cost: Int, heuristic: Int)
        suspend fun shortCut(oldFromState: S, oldToState: S, newFromState: S, newToState: S)
    }
}
