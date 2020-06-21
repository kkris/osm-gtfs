package org.kkris.osmgtfs.common.model.route

import io.leonard.Position
import org.kkris.osmgtfs.common.model.Coordinate
import org.kkris.osmgtfs.common.model.stop.Stop

data class Route(
    val id: Long,
    val name: String,
    val color: String,
    val stops: List<Stop>,
    val path: List<Coordinate>
) {
    fun origin(): Stop {
        return stops.first()
    }

    fun destination(): Stop {
        return stops.last()
    }

    fun polyline(): String {
        return io.leonard.PolylineUtils.encode(path.map { Position.fromLngLat(it.longitude, it.latitude) }, 5)
    }
}
