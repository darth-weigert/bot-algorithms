package br.darthweigert.geometry

import korlibs.math.geom.Point
import korlibs.math.geom.shape.buildVectorPath
import korlibs.math.geom.vector.VectorPath

fun List<Point>.toVectorPath(): VectorPath {
    return buildVectorPath {
        val start = first()
        moveTo(start)
        for (point in asSequence().drop(1)) {
            lineTo(point)
        }
        // close polygon
        lineTo(start)
    }
}
