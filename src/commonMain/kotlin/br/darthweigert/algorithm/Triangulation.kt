package br.darthweigert.algorithm

import br.darthweigert.geometry.Polygon
import br.darthweigert.geometry.Triangle
import korlibs.datastructure.getCyclic
import korlibs.math.geom.Point

class Triangulation(private val debug: Debug = Debug.NO_OP) {

    enum class Winding {
        CLOCKWISE, COUNTERCLOCKWISE
    }

    suspend fun triangulate(polygon: Polygon): List<Triangle> {
        debug.start(polygon)
        val points = bridgeHoles(polygon)
        debug.intermediatePolygon(points)
        return earClipping(points)
    }

    suspend fun earClipping(points: List<Point>): List<Triangle> {
        if (points.size < 3) {
            return listOf()
        }

        val winding = determineWinding(points)
        val indices = points.indices.toMutableList()
        val triangles = mutableListOf<Triangle>()

        while(indices.size > 2) {
            val (index, ear) = indices.indices.asSequence()
                .map { loop ->
                    val prev = indices.getCyclic(loop - 1)
                    val index = indices.getCyclic(loop)
                    val next = indices.getCyclic(loop + 1)
                    index to Triangle(points.getCyclic(prev), points.getCyclic(index), points.getCyclic(next))
                }
                .filter { (_, triangle) -> isConvex(triangle.b, triangle.c, triangle.a, winding) }
                .filter { (_, triangle) -> indices.asSequence().map { points[it] }.filter { it !in triangle.points }.none { it in triangle } }
                .minBy { (index, triangle) ->
                    debug.earCandidate(index, triangle)
                    val (v1, _, v3) = triangle
                    (v3 - v1).length
                }
            debug.earToClip(index, ear)
            indices.remove(index)
            triangles.add(ear)
        }
        return triangles
    }

    suspend fun bridgeHoles(polygon: Polygon): ArrayList<Point> {
        val points = ArrayList(polygon.points)
        val polygonWinding = determineWinding(polygon.points)
        for (hole in polygon.holes) {
            val holeWinding = determineWinding(hole.points)
            val (_, closestHolePoint, closestBorderPoint) = hole.points.asSequence()
                .map { holePoint ->
                    val (distance, borderPoint) = polygon.points.asSequence()
                        .map { point ->
                            val distance = (holePoint - point).length
                            distance to point
                        }
                        .minBy { (distance, _) -> distance }
                    Triple(distance, holePoint, borderPoint)
                }
                .minBy { (distance, _, _) -> distance }
            val closestHolePointIndex = hole.points.indexOf(closestHolePoint)
            val closestBorderPointIndex = points.indexOf(closestBorderPoint)
            debug.bridgeHole(closestBorderPoint, closestHolePoint)
            var targetIndex = closestBorderPointIndex + 1
            val indices = (hole.points.indices.asSequence() + sequenceOf(0))
                .map { (it + closestHolePointIndex) % hole.points.size }
                .toList()
                .let { if (polygonWinding == holeWinding) it.reversed() else it }
            for (sourceIndex in indices) {
                points.add(targetIndex++, hole.points[sourceIndex])
            }
            points.add(targetIndex, closestBorderPoint)
        }
        return points
    }

    fun determineWinding(points: List<Point>): Winding {
        val edges = points.asSequence().zipWithNext() + sequenceOf(points.last() to points.first())
        val sum = edges.sumOf { (a, b) -> (b.x - a.x)*(b.y + a.y) }
        return if (sum < 0) {
            Winding.COUNTERCLOCKWISE
        } else {
            Winding.CLOCKWISE
        }
    }

    private fun isConvex(p1: Point, p2: Point, p3: Point, winding: Winding): Boolean {
        val a = p2 - p1
        val b = p3 - p1

        val crossProduct = a.cross(b)

        return when (winding) {
            Winding.CLOCKWISE -> crossProduct < 0
            Winding.COUNTERCLOCKWISE -> crossProduct > 0
        }
    }

//    private fun <T> List<T>.getItem(index: Int): T {
//        return this[(index + size) % size]
//    }

    interface Debug {
        companion object {
            val NO_OP = object: Debug {
                override suspend fun start(polygon: Polygon) { }
                override suspend fun bridgeHole(borderPoint: Point, holePoint: Point) { }
                override suspend fun intermediatePolygon(points: List<Point>) { }
                override suspend fun earCandidate(index: Int, ear: Triangle) { }
                override suspend fun earToClip(index: Int, ear: Triangle) { }
            }
        }
        suspend fun start(polygon: Polygon)
        suspend fun bridgeHole(borderPoint: Point, holePoint: Point)
        suspend fun intermediatePolygon(points: List<Point>)
        suspend fun earCandidate(index: Int, ear: Triangle)
        suspend fun earToClip(index: Int, ear: Triangle)
    }
}
