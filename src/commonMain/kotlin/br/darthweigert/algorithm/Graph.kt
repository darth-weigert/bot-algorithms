package br.darthweigert.algorithm

import kotlinx.collections.immutable.toImmutableSet

interface Graph<NODE, EDGE> {
    fun add(node: NODE)
    fun add(node1: NODE, node2: NODE, value: EDGE)

    fun getNodes(): Set<NODE>
    fun getLinks(node: NODE): Set<NODE>?
    fun getEdge(node1: NODE, node2: NODE): EDGE?
    fun remove(node: NODE)
    fun remove(node1: NODE, node2: NODE)

    class UndirectedGraph<NODE, EDGE>: Graph<NODE, EDGE> {

        private val nodes: MutableSet<NODE> = mutableSetOf()
        private val edges: MutableMap<Pair<NODE, NODE>, EDGE> = mutableMapOf()
        private val map: MutableMap<NODE, MutableSet<NODE>> = mutableMapOf()

        override fun add(node: NODE) {
            if (nodes.add(node)) {
                map[node] = mutableSetOf()
            }
        }

        override fun add(node1: NODE, node2: NODE, value: EDGE) {
            add(node1)
            add(node2)
            edges[Pair(node1, node2)] = value
            map(node1, node2)
            map(node2, node1)
        }

        override fun getNodes(): Set<NODE> {
            return nodes.toImmutableSet()
        }

        override fun getLinks(node: NODE): Set<NODE>? {
            return map[node]?.toImmutableSet()
        }

        override fun getEdge(node1: NODE, node2: NODE): EDGE? {
            val key = Pair(node1, node2)
            return edges[key]
        }

        override fun remove(node: NODE) {
            if (node in nodes) {
                for (other in map[node]!!) {
                    unmap(other, node)
                }
                edges.keys.removeAll { it.first == node || it.second == node }
                map.remove(node)
                nodes.remove(node)
            }
        }

        override fun remove(node1: NODE, node2: NODE) {
            edges.remove(Pair(node1, node2))
            unmap(node1, node2)
            unmap(node2, node1)
        }

        private fun map(node1: NODE, node2: NODE) {
            map[node1]!!.add(node2)
        }

        private fun unmap(node1: NODE, node2: NODE) {
            map[node1]!!.remove(node2)
        }
    }
}
