package br.darthweigert.geometry

import korlibs.math.geom.Point

data class Polygon(val points: List<Point>, val holes: List<PolygonHole> = listOf())

data class PolygonHole(val points: List<Point>)
