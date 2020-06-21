package org.kkris.osmgtfs.gtfs.model

import org.kkris.osmgtfs.common.model.Coordinate
import org.onebusaway.gtfs.model.AgencyAndId
import org.onebusaway.gtfs.model.ShapePoint

// TODO: provide distTraveled
data class GtfsShapePoint(
    val coordinate: Coordinate,
    val shapeId: String,
    val sequence: Int
): GtfsEntity<ShapePoint>() {
    override fun toEntity(agency: GtfsAgency): ShapePoint {
        val point = ShapePoint()

        point.shapeId = AgencyAndId(agency.id, shapeId)
        point.lat = coordinate.latitude
        point.lon = coordinate.longitude
        point.sequence = sequence

        return point
    }

    override fun id(): String {
        throw UnsupportedOperationException("ShapePoint has no id")
    }
}