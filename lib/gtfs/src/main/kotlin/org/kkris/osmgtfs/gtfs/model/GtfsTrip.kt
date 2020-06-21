package org.kkris.osmgtfs.gtfs.model

import org.kkris.osmgtfs.common.identifier
import org.kkris.osmgtfs.common.model.Coordinate
import org.kkris.osmgtfs.common.model.TripOperationType
import org.kkris.osmgtfs.common.model.TripOperationType.REGULAR
import org.kkris.osmgtfs.common.model.TripOperationType.SHORT
import org.kkris.osmgtfs.common.model.stop.Stop
import org.kkris.osmgtfs.common.model.trip.TripStop
import org.kkris.osmgtfs.gtfs.util.SequentialIdGenerator
import org.onebusaway.gtfs.model.AgencyAndId
import org.onebusaway.gtfs.model.Trip

data class GtfsTrip(
    val route: GtfsRoute,
    val origin: Stop,
    val destination: Stop,
    val stops: List<TripStop>,
    val path: List<Coordinate>,
    val calendar: GtfsCalendar,
    val tripNumber: Int,
    val tripType: TripOperationType?
): GtfsEntity<Trip>() {
    companion object {
        private val baseIdGenerator = SequentialIdGenerator(1_000)
        private val tripIdGenerator = SequentialIdGenerator(1_000)
    }

    override fun toEntity(agency: GtfsAgency): Trip {
        val entity = Trip()

        entity.id = AgencyAndId(agency.id, id())
        entity.route = route.toEntity(agency)
        entity.serviceId = AgencyAndId(agency.id, calendar.id())
        entity.tripShortName = route.name
        entity.tripHeadsign = destination.name
        entity.shapeId = AgencyAndId(agency.id, shapeId())

        return entity
    }

    override fun id(): String {
        return "trip:${baseId()}-${tripIdGenerator.getId(tripNumber)}".identifier()
    }

    fun getShape(): List<GtfsShapePoint> {
        return path.mapIndexed { index, coordinate ->
            GtfsShapePoint(
                coordinate,
                shapeId(),
                index
            )
        }
    }

    private fun shapeId(): String {
        return "path:${baseId()}".identifier() // Note: without tripNumber
    }

    private fun baseId(): String {
        val seq = baseIdGenerator.getId(route, origin, destination, calendar, tripType)

        val type = when (tripType) {
            SHORT -> "-short"
            REGULAR -> "-regular"
            else -> ""
        }

        return "route-${route.name}-${calendar.id()}$type-$seq"
    }
}