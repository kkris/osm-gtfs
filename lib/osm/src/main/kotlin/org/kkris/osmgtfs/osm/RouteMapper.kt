package org.kkris.osmgtfs.osm

import org.kkris.osmgtfs.common.Util
import org.kkris.osmgtfs.common.model.Coordinate
import org.kkris.osmgtfs.common.model.route.Route
import org.kkris.osmgtfs.common.model.stop.Stop
import org.kkris.osmgtfs.osm.model.OsmRoute

object RouteMapper {
    internal fun mapRoute(route: OsmRoute, stops: List<Stop>): Route {
        val sequence = route.sequence.map { routeStop ->
            stops.find { it.osmId == routeStop.osmId } ?: throw IllegalStateException("Failed to find stop with given osmId")
        }

        val origin = sequence.first()
        val path = mapPath(route.path, origin)

        return Route(
            route.osmId,
            route.line.name,
            route.line.color,
            sequence,
            path
        )
    }

    private fun mapPath(path: List<Long>, origin: Stop): List<Coordinate> {
        val cache = OsmEntityCache.get()

        // convert each way to a segment
        // a segment is a list of node coordinates
        val segments = path
            .mapNotNull {
                cache.getWay(it)?.nodes?.toArray()?.toList()
            }
            .map { nodeIds ->
                nodeIds.mapNotNull { cache.getNode(it)?.getCoordinate() }
            }
            .map {
                Segment(it.toMutableList())
            }

        // correctly orient segments such that each segment directly follows its predecessor
        // find segments which are oriented wrongly by checking the endpoints of each segment with the last node of the
        // previous segment. if the first endpoint is farther away than the last endpoint, reverse the node order
        // use origin as initial pivot for the first segment
        val pivot = Segment(mutableListOf(origin.coordinate))

        (listOf(pivot) + segments).zipWithNext { a, b ->
            val d1 = Util.distance(a.last(), b.first())
            val d2 = Util.distance(a.last(), b.last())

            if (d2 < d1) {
                // segment b is flipped (b.last is closer to a.last instead of b.first being closer to a.last)
                b.reverse()
            }
        }

        return segments.flatMap {
            it.nodes
        }
    }
}


private data class Segment(
    val nodes: MutableList<Coordinate>
) {
    fun first(): Coordinate {
        return nodes.first()
    }

    fun last(): Coordinate {
        return nodes.last()
    }

    fun reverse() {
        nodes.reverse()
    }
}