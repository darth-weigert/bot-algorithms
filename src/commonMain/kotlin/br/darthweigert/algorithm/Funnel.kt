package br.darthweigert.algorithm

import br.darthweigert.geometry.Triangle
import korlibs.math.geom.Line
import korlibs.math.geom.Point
import kotlin.math.min

class Funnel(private val debug: Debug = Debug.NO_OP) {
    suspend fun find(channel: List<Triangle>, pStart: Point, pEnd: Point): List<Point> {
        if (channel.isEmpty()) return listOf()
        val result = mutableListOf<Point>()
        if (channel.size == 1) {
            result.add(pStart)
            result.add(pEnd)
            return result
        }
        val portals = mutableListOf<Line>()
        val channelIterator = channel.iterator()
        var state = channelIterator.next()

        var next = channelIterator.next()
        var edge = state.sharedEdge(next)

        val A = pStart
        val B = edge.center
        val a = B - A
        val b = edge.a - A
        var (left, right) = if (a.cross(b) < 0) {
            edge.b to edge.a
        } else {
            edge.a to edge.b
        }
        portals.add(Line(left, right))
        debug.portal(left, right)
        state = next
        do {
            next = channelIterator.next()
            edge = state.sharedEdge(next)

            when (left) {
                edge.a -> { right = edge.b }
                edge.b -> { right = edge.a }
                else -> { left = if (right == edge.a) edge.b else edge.a }
            }
            portals.add(Line(left, right))
            debug.portal(left, right)
            state = next
        } while (channelIterator.hasNext())
        portals.add(Line(pEnd, pEnd))
        return stringPull(pStart, pEnd, portals)
    }

    private suspend fun stringPull(pStart: Point, pEnd: Point, portals: List<Line>): List<Point> {
        val result = mutableListOf<Point>()

        var portalApex = pStart
        var portalLeft = portals[0].a
        var portalRight = portals[0].b

        var leftIndex = 0
        var rightIndex = 0
        var funnelLeft = portalLeft - portalApex
        var funnelRight = portalRight - portalApex
        debug.point(portalApex)
        result.add(portalApex)

        var index = 1

        fun reset(apexIndex: Int, newPortalApex: Point) {
            leftIndex = apexIndex
            portalApex = newPortalApex
            do { portalLeft = portals[++leftIndex].a } while (portalLeft == portalApex)
            rightIndex = apexIndex
            do { portalRight = portals[++rightIndex].b } while (portalRight == portalApex)
            funnelLeft = portalLeft - portalApex
            funnelRight = portalRight - portalApex
            index = min(leftIndex, rightIndex)
        }

        while (index < portals.size) {
            debug.currentFunnel(portalApex, portalLeft, portalRight)
            val (left, right) = portals[index]
            debug.portal(left, right)
            // If new left vertex is different, process.
            if (portalLeft != left && index > leftIndex) {
                val newSide = left - portalApex
                // If new side does not widen funnel, update.
                if (newSide.cross(funnelLeft) >= 0) {
                    // If new side crosses other side, update apex.
                    if (newSide.cross(funnelRight) > 0) {
                        debug.funnelCrossed(portalApex, portalLeft to left, portalRight to portalRight)
                        // add
                        debug.point(portalRight)
                        result.add(portalRight)
                        // reset
                        reset(rightIndex, portalRight)
                        continue
                    } else {
                        leftIndex = index
                        portalLeft = left
                        funnelLeft = portalLeft - portalApex
                    }
                }
            }
            // If new right vertex is different, process.
            if (portalRight != right && index > rightIndex) {
                val newSide = right - portalApex
                // If new side does not widen funnel, update.
                if (newSide.cross(funnelRight) <= 0) {
                    // If new side crosses other side, update apex.
                    if (newSide.cross(funnelLeft) < 0) {
                        debug.funnelCrossed(portalApex, portalLeft to portalLeft, portalRight to right)
                        // add
                        debug.point(portalLeft)
                        result.add(portalLeft)
                        // reset
                        reset(leftIndex, portalLeft)
                        continue
                    } else {
                        rightIndex = index
                        portalRight = right
                        funnelRight = portalRight - portalApex
                    }
                }
            }
            ++index
        }
        result.add(pEnd)
        return result
    }

    interface Debug {
        companion object {
            val NO_OP = object: Debug {
                override suspend fun portal(left: Point, right: Point) { }
                override suspend fun currentFunnel(portalApex: Point, portalLeft: Point, portalRight: Point) { }
                override suspend fun point(point: Point) { }
                override suspend fun funnelCrossed(portalApex: Point, leftChange: Pair<Point, Point>, rightChange: Pair<Point, Point>) { }
            }
        }
        suspend fun portal(left: Point, right: Point)
        suspend fun currentFunnel(portalApex: Point, portalLeft: Point, portalRight: Point)
        suspend fun point(point: Point)
        suspend fun funnelCrossed(portalApex: Point, leftChange: Pair<Point, Point>, rightChange: Pair<Point, Point>)
    }
}
