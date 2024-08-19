package br.darthweigert.geometry

import korlibs.math.geom.Line2D
import korlibs.math.geom.Point
import korlibs.math.geom.Vector2D

data class Triangle(val a: Point, val b: Point, val c: Point) {
    val points = listOf(a, b, c)

    fun closestPoint(point: Vector2D): Vector2D {
        val A = a
        val B = b
        val C = c
        val P = point
        val edge0 = B - A
        val edge1 = C - A
        val v0 = A - P
        val a = edge0.dot(edge0)
        val b = edge0.dot(edge1)
        val c = edge1.dot(edge1)
        val d = edge0.dot(v0)
        val e = edge1.dot(v0)
        val det = a * c - b * b
        var s = b * e - c * d
        var t = b * d - a * e
        if (s + t < det) {
            if (s < 0.0) {
                if (t < 0.0) {
                    if (d < 0.0) {
                        s = (-d / a).coerceIn(0.0, 1.0)
                        t = 0.0
                    } else {
                        s = 0.0
                        t = (-e / c).coerceIn(0.0, 1.0)
                    }
                } else {
                    s = 0.0
                    t = (-e / c).coerceIn(0.0, 1.0)
                }
            } else if (t < 0.0) {
                s = (-d / a).coerceIn(0.0, 1.0)
                t = 0.0
            } else {
                val invDet = 1.0 / det
                s *= invDet
                t *= invDet
            }
        } else {
            if (s < 0.0) {
                val tmp0 = b + d
                val tmp1 = c + e
                if (tmp1 > tmp0) {
                    val numer = tmp1 - tmp0
                    val denom = a - 2 * b + c
                    s = (numer / denom).coerceIn(0.0, 1.0)
                    t = 1 - s
                } else {
                    t = (-e / c).coerceIn(0.0, 1.0)
                    s = 0.0
                }
            } else if (t < 0.0) {
                if (a + d > b + e) {
                    val numer = c + e - b - d
                    val denom = a - 2 * b + c
                    s = (numer / denom).coerceIn(0.0, 1.0)
                    t = 1 - s
                } else {
                    s = (-e / c).coerceIn(0.0, 1.0)
                    t = 0.0
                }
            } else {
                val numer = c + e - b - d
                val denom = a - 2 * b + c
                s = (numer / denom).coerceIn(0.0, 1.0)
                t = 1.0 - s
            }
        }

        return (A + edge0 * s) + edge1 * t
    }

    fun sharedEdge(other: Triangle): Line2D {
        val pointA = a in other.points
        val pointB = b in other.points
        val pointC = c in other.points
        if (pointA && pointB) {
            return Line2D(a, b)
        }
        if (pointA && pointC) {
            return Line2D(a, c)
        }
        if (pointB && pointC) {
            return Line2D(b, c)
        }
        throw IllegalArgumentException("Triangles do not share an edge")
    }

    val centroid: Vector2D
        get() {
            return Vector2D((a.x + b.x + c.x) / 3.0, (a.y + b.y + c.y) / 3.0)
        }

    operator fun contains(pt: Point): Boolean {
        fun sign(p1: Point, p2: Point, p3: Point): Double {
            return (p1.x - p3.x) * (p2.y - p3.y) - (p2.x - p3.x) * (p1.y - p3.y)
        }

        val v1 = a
        val v2 = b
        val v3 = c

        val d1 = sign(pt, v1, v2)
        val d2 = sign(pt, v2, v3)
        val d3 = sign(pt, v3, v1)

        val hasNeg = (d1 < 0) || (d2 < 0) || (d3 < 0)
        val hasPos = (d1 > 0) || (d2 > 0) || (d3 > 0)

        return !(hasNeg && hasPos)
    }
}
