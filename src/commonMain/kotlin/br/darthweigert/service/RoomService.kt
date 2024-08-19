package br.darthweigert.service

import br.darthweigert.geometry.Polygon
import br.darthweigert.geometry.Triangle
import br.darthweigert.algorithm.Triangulation
import br.darthweigert.geometry.PolygonHole
import br.darthweigert.geometry.toVectorPath
import br.darthweigert.math.nextRandomGray
import korlibs.korge.view.Container
import korlibs.korge.view.solidRect
import korlibs.math.geom.Point
import korlibs.math.geom.Vector2I
import korlibs.math.geom.vector.VectorPath
import urbanistic.clipper.ClipType
import urbanistic.clipper.Clipper
import urbanistic.clipper.LongPoint
import urbanistic.clipper.Path
import urbanistic.clipper.Paths
import urbanistic.clipper.PolyFillType
import urbanistic.clipper.PolyNode
import urbanistic.clipper.PolyTree
import urbanistic.clipper.PolyType
import urbanistic.clipper.pathOf
import kotlin.math.max
import kotlin.math.min
import kotlin.random.Random

class RoomService(private val random: Random) {

    companion object {
        const val SCALE_LONG = 1L
        const val SCALE_DOUBLE = SCALE_LONG.toDouble()
    }

    private val rooms = Paths()

    fun clear() {
        rooms.clear()
    }

    fun addRoom(start: Vector2I, end: Vector2I) {
        val p1x: Long = start.x * SCALE_LONG
        val p1y: Long = start.y * SCALE_LONG
        val p2x: Long = end.x * SCALE_LONG
        val p2y: Long = end.y * SCALE_LONG
        val left = min(p1x, p2x)
        val top = min(p1y, p2y)
        val right = max(p1x, p2x)
        val bottom = max(p1y, p2y)
        rooms.add(pathOf(
            LongPoint(left, top),
            LongPoint(right, top),
            LongPoint(right, bottom),
            LongPoint(left, bottom)
        ))
    }

    fun draw(shapeContainer: Container) {
        for (box in rooms) {
            val left = box[0].x
            val top = box[0].y
            val right = box[2].x
            val bottom = box[2].y
            shapeContainer.solidRect((right - left).toDouble(), (bottom - top).toDouble(), random.nextRandomGray()) {
                x = left.toDouble()
                y = top.toDouble()
            }
        }
    }

    fun roomsOutlines(): List<VectorPath> {
        val clipper = Clipper()
        clipper.addPaths(rooms, PolyType.Subject, true)
        val result = PolyTree()
        clipper.execute(ClipType.Union, result, PolyFillType.Positive, PolyFillType.Negative)

        return result.allChildren().map { it.contour.toPointList().toVectorPath() }.toList()
    }

    suspend fun createNavMesh(debug: Triangulation.Debug): List<Triangle> {
        val clipper = Clipper()
        clipper.addPaths(rooms, PolyType.Subject, true)
        val result = PolyTree()
        clipper.execute(ClipType.Union, result, PolyFillType.Positive, PolyFillType.Negative)

        val triangles = mutableListOf<Triangle>()
        for (polyNode in result.allChildren().filter { !it.isHole }) {
            val points = polyNode.contour.toPointList()
            val holes = polyNode.childs.asSequence()
                .filter { it.isHole }
                .map { PolygonHole(it.contour.toPointList()) }
                .toList()

            triangles.addAll(
                Triangulation(debug).triangulate(
                    Polygon(
                        points,
                        holes
                    )
                ))
        }
        return triangles
    }

    private fun PolyNode.allChildren(): Sequence<PolyNode> {
        return this.childs.asSequence().flatMap { sequenceOf(it) + it.allChildren() }
    }

    private fun LongPoint.toPoint(): Point {
        return Point(x / SCALE_DOUBLE, y / SCALE_DOUBLE)
    }

    private fun Path.toPointList(): List<Point> {
        return this.asSequence().map {
            it.toPoint()
        }.toList()
    }
}
