package br.darthweigert.algorithm

import br.darthweigert.geometry.Polygon
import br.darthweigert.geometry.PolygonHole
import br.darthweigert.geometry.Triangle
import io.kotest.matchers.shouldBe
import korlibs.datastructure.doubleArrayListOf
import korlibs.image.bitmap.NativeImageContext2d
import korlibs.image.color.Colors
import korlibs.image.format.PNG
import korlibs.image.format.writeTo
import korlibs.io.async.suspendTest
import korlibs.io.file.std.uniVfs
import korlibs.math.geom.Line
import korlibs.math.geom.Point
import korlibs.math.geom.Vector2D
import kotlin.test.Test

class TriangulationTest {

    private val triangulation = Triangulation()

    @Test
    fun simplePolygon() = suspendTest {
        val points = listOf(
            Point(-4.0, 6.0),
            Point(0.0, 2.0),
            Point(2.0, 5.0),
            Point(7.0, 0.0),
            Point(5.0, -6.0),
            Point(3.0, 3.0),
            Point(0.0, -5.0),
            Point(-6.0, 0.0),
            Point(-2.0, 1.0)
        )
        val triangles = triangulation.earClipping(points)

        debugDraw(Polygon(points), triangles, "/tmp/image-simple1.png")

        triangles shouldBe listOf(
            Triangle(points[8], points[0], points[1]),
            Triangle(points[3], points[4], points[5]),
            Triangle(points[2], points[3], points[5]),
            Triangle(points[1], points[2], points[5]),
            Triangle(points[8], points[1], points[5]),
            Triangle(points[8], points[5], points[6]),
            Triangle(points[8], points[6], points[7])
        )
    }

    @Test
    fun simplePolygonReverseWinding() = suspendTest {
        val points = listOf(
            Point(-4.0, 6.0),
            Point(-2.0, 1.0),
            Point(-6.0, 0.0),
            Point(0.0, -5.0),
            Point(3.0, 3.0),
            Point(5.0, -6.0),
            Point(7.0, 0.0),
            Point(2.0, 5.0),
            Point(0.0, 2.0)
        )
        val triangles = triangulation.earClipping(points)

        debugDraw(Polygon(points), triangles, "/tmp/image-simple2.png")

        triangles shouldBe listOf(
            Triangle(points[8], points[0], points[1]),
            Triangle(points[4], points[5], points[6]),
            Triangle(points[4], points[6], points[7]),
            Triangle(points[4], points[7], points[8]),
            Triangle(points[4], points[8], points[1]),
            Triangle(points[1], points[2], points[3]),
            Triangle(points[1], points[3], points[4])
        )
    }

    @Test
    fun polygonWithHoles() = suspendTest {
        val points = listOf(
            Point(4.0, 2.5), // 0
            Point(5.0, 5.0),
            Point(4.0, 6.0),
            Point(1.0, 5.5),
            Point(0.0, 9.5),
            Point(5.0, 11.0), // 5 <- bridge with hole1 here
            Point(3.0, 14.0),
            Point(9.0, 14.0),
            Point(10.0, 8.0),
            Point(12.0, 9.5),
            Point(11.0, 12.0), // 10
            Point(14.0, 15.0),
            Point(18.0, 13.0),
            Point(21.0, 9.0),
            Point(17.0, 10.0), // 14 <- bridge with hole2 here
            Point(16.0, 6.5), // 15 <- bridge with hole3 here
            Point(19.0, 6.0),
            Point(20.0, 5.0),
            Point(19.0, 2.0),
            Point(15.0, 0.0),
            Point(12.0, 1.0), // 20
            Point(12.5, 3.0),
            Point(11.0, 4.0),
            Point(7.0, 3.0),
            Point(8.0, 1.0),
            Point(6.0, 0.0) // 25
        )

        val hole1 = listOf(
            Point(4.0, 8.5),
            Point(6.0, 9.5),
            Point(7.0, 7.5)
        )

        val hole2 = listOf(
            Point(15.0, 10.0),
            Point(14.0, 11.5),
            Point(16.0, 12.5)
        )

        val hole3 = listOf(
            Point(15.0, 6.0),
            Point(16.0, 3.0),
            Point(13.0, 5.0)
        )

        val polygon = Polygon(points, listOf(PolygonHole(hole1), PolygonHole(hole2), PolygonHole(hole3)))

        val triangles = triangulation.triangulate(polygon)

        debugDraw(polygon, triangles, "/tmp/image-withHoles.png")

        triangles shouldBe listOf(
            Triangle(points[5], hole1[1], hole1[0]),
            Triangle(points[14], hole2[0], hole2[2]),
            Triangle(points[12], points[13], points[14]),
            Triangle(points[12], points[14], hole2[2]),
            Triangle(points[23], points[24], points[25]),
            Triangle(points[23], points[25], points[0]),
            Triangle(points[23], points[0], points[1]),
            Triangle(points[11], points[12], hole2[2]),
            Triangle(points[11], hole2[2], hole2[1]),
            Triangle(points[10], points[11], hole2[1]),
            Triangle(points[9], points[10], hole2[1]),
            Triangle(points[9], hole2[1], hole2[0]),
            Triangle(hole3[1], hole3[0], points[15]),
            Triangle(hole2[0], points[14], points[15]),
            Triangle(points[19], points[20], points[21]),
            Triangle(hole2[0], points[15], hole3[0]),
            Triangle(points[16], points[17], points[18]),
            Triangle(points[4], points[5], hole1[0]),
            Triangle(points[3], points[4], hole1[0]),
            Triangle(points[2], points[3], hole1[0]),
            Triangle(points[2], hole1[0], hole1[2]),
            Triangle(points[1], points[2], hole1[2]),
            Triangle(hole3[1], points[15], points[16]),
            Triangle(hole3[1], points[16], points[18]),
            Triangle(hole3[1], points[18], points[19]),
            Triangle(hole3[1], points[19], points[21]),
            Triangle(hole3[2], hole3[1], points[21]),
            Triangle(hole3[2], points[21], points[22]),
            Triangle(points[23], points[1], hole1[2]),
            Triangle(points[9], hole2[0], hole3[0]),
            Triangle(points[9], hole3[0], hole3[2]),
            Triangle(points[8], points[9], hole3[2]),
            Triangle(points[8], hole3[2], points[22]),
            Triangle(points[5], points[6], points[7]),
            Triangle(points[22], points[23], hole1[2]),
            Triangle(points[8], points[22], hole1[2]),
            Triangle(points[8], hole1[2], hole1[1]),
            Triangle(hole1[1], points[5], points[7]),
            Triangle(hole1[1], points[7], points[8])
        )
    }

    @Test
    fun polygonCWHoleCW() = suspendTest {
        val points = listOf(
            Point(0.0, 0.0),
            Point(0.0, 10.0),
            Point(10.0, 10.0),
            Point(10.0, 0.0)
        )

        val hole = listOf(
            Point(5.0, 2.0),
            Point(2.0, 5.0),
            Point(5.0, 8.0),
            Point(8.0, 5.0)
        )

        triangulation.determineWinding(points) shouldBe Triangulation.Winding.CLOCKWISE
        triangulation.determineWinding(hole) shouldBe Triangulation.Winding.CLOCKWISE

        val polygon = Polygon(points, listOf(PolygonHole(hole)))
        val triangles = triangulation.triangulate(polygon)

        debugDraw(polygon, triangles, "/tmp/image-CW_CW.png")

        triangles shouldBe listOf(
            Triangle(points[3], points[0], hole[0]),
            Triangle(points[3], hole[0], hole[3]),
            Triangle(hole[1], hole[0], points[0]),
            Triangle(hole[1], points[0], points[1]),
            Triangle(hole[2], hole[1], points[1]),
            Triangle(hole[2], points[1], points[2]),
            Triangle(hole[3], hole[2], points[2]),
            Triangle(hole[3], points[2], points[3]),
        )
    }

    @Test
    fun polygonCWHoleCCW() = suspendTest {
        val points = listOf(
            Point(0.0, 0.0),
            Point(0.0, 10.0),
            Point(10.0, 10.0),
            Point(10.0, 0.0)
        )

        val hole = listOf(
            Point(5.0, 2.0),
            Point(8.0, 5.0),
            Point(5.0, 8.0),
            Point(2.0, 5.0)
        )

        triangulation.determineWinding(points) shouldBe Triangulation.Winding.CLOCKWISE
        triangulation.determineWinding(hole) shouldBe Triangulation.Winding.COUNTERCLOCKWISE

        val polygon = Polygon(points, listOf(PolygonHole(hole)))
        val triangles = triangulation.triangulate(polygon)

        debugDraw(polygon, triangles, "/tmp/image-CW_CCW.png")

        triangles shouldBe listOf(
            Triangle(points[3], points[0], hole[0]),
            Triangle(points[3], hole[0], hole[1]),
            Triangle(hole[3], hole[0], points[0]),
            Triangle(hole[3], points[0], points[1]),
            Triangle(hole[2], hole[3], points[1]),
            Triangle(hole[2], points[1], points[2]),
            Triangle(hole[1], hole[2], points[2]),
            Triangle(hole[1], points[2], points[3]),
        )
    }

    @Test
    fun polygonCCWHoleCW() = suspendTest {
        val points = listOf(
            Point(0.0, 0.0),
            Point(10.0, 0.0),
            Point(10.0, 10.0),
            Point(0.0, 10.0)
        )

        val hole = listOf(
            Point(5.0, 2.0),
            Point(2.0, 5.0),
            Point(5.0, 8.0),
            Point(8.0, 5.0)
        )

        triangulation.determineWinding(points) shouldBe Triangulation.Winding.COUNTERCLOCKWISE
        triangulation.determineWinding(hole) shouldBe Triangulation.Winding.CLOCKWISE

        val polygon = Polygon(points, listOf(PolygonHole(hole)))
        val triangles = triangulation.triangulate(polygon)

        debugDraw(polygon, triangles, "/tmp/image-CCW_CW.png")

        triangles shouldBe listOf(
            Triangle(points[0], hole[0], hole[1]),
            Triangle(points[3], points[0], hole[1]),
            Triangle(points[3], hole[1], hole[2]),
            Triangle(hole[0], points[0], points[1]),
            Triangle(hole[3], hole[0], points[1]),
            Triangle(hole[3], points[1], points[2]),
            Triangle(hole[2], hole[3], points[2]),
            Triangle(hole[2], points[2], points[3])
        )
    }

    @Test
    fun polygonCCWHoleCCW() = suspendTest {
        val points = listOf(
            Point(0.0, 0.0),
            Point(10.0, 0.0),
            Point(10.0, 10.0),
            Point(0.0, 10.0)
        )

        val hole = listOf(
            Point(5.0, 2.0),
            Point(8.0, 5.0),
            Point(5.0, 8.0),
            Point(2.0, 5.0)
        )

        triangulation.determineWinding(points) shouldBe Triangulation.Winding.COUNTERCLOCKWISE
        triangulation.determineWinding(hole) shouldBe Triangulation.Winding.COUNTERCLOCKWISE

        val polygon = Polygon(points, listOf(PolygonHole(hole)))
        val triangles = triangulation.triangulate(polygon)

        debugDraw(polygon, triangles, "/tmp/image-CCW_CCW.png")

        triangles shouldBe listOf(
            Triangle(points[0], hole[0], hole[3]),
            Triangle(points[3], points[0], hole[3]),
            Triangle(points[3], hole[3], hole[2]),
            Triangle(hole[0], points[0], points[1]),
            Triangle(hole[1], hole[0], points[1]),
            Triangle(hole[1], points[1], points[2]),
            Triangle(hole[2], hole[1], points[2]),
            Triangle(hole[2], points[2], points[3])
        )
    }

    private suspend fun debugDraw(polygon: Polygon, triangles: List<Triangle>, fileName: String) {
        val scale = 40.0
        val (points, holes) = polygon
        val allPoints = (points.asSequence() + holes.asSequence().flatMap { it.points }).map { it * scale }.toList()
        val minX = allPoints.minOf { it.x }
        val minY = allPoints.minOf { it.y }
        val offset = Vector2D(-minX, -minY)
        NativeImageContext2d(850, 600) {
            fill(Colors.BLACK) {
                polygon(points.map { it * scale + offset })
            }
            fill(Colors.DARKGRAY) {
                for (hole in holes) {
                    polygon(hole.points.map { it * scale + offset })
                }
            }
            stroke(Colors.WHITE, 2f, lineDash = doubleArrayListOf(5.0, 5.0)) {
                val lines = triangles.asSequence()
                    .flatMap { triangle ->
                        val (a, b, c) = triangle.points.sortedWith(compareBy({ it.x }, { it.y }))
                        listOf(Line(a, b), Line(a, c), Line(b, c))
                    }
                    .toSet()
                for (line in lines) {
                    line(line.a * scale + offset, line.b * scale + offset)
                }
            }
            stroke(Colors.WHITE, 4f) {
                polygon(points.map { it * scale + offset })
            }
            stroke(Colors.WHITE, 4f) {
                for (hole in holes) {
                    polygon(hole.points.map { it * scale + offset })
                }
            }
        }.writeTo(fileName.uniVfs, PNG)
    }
}
