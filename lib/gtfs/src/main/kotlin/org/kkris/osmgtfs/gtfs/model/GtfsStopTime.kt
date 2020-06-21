package org.kkris.osmgtfs.gtfs.model

import org.onebusaway.gtfs.model.StopTime
import java.time.LocalTime
import javax.naming.OperationNotSupportedException

data class GtfsStopTime(
    val trip: GtfsTrip,
    val stop: GtfsStop,
    val departure: LocalTime? = null,
    val arrival: LocalTime? = null,
    val sequence: Int
): GtfsEntity<StopTime>() {
    override fun toEntity(agency: GtfsAgency): StopTime {
        val entity = StopTime()

        entity.trip = trip.toEntity(agency)

        if (departure != null && arrival != null) {
            entity.departureTime = departure.toSecondOfDay()
            entity.arrivalTime = arrival.toSecondOfDay()
        } else {
            departure?.let {
                entity.arrivalTime = it.toSecondOfDay()
                entity.departureTime = it.toSecondOfDay()
            }
            arrival?.let {
                entity.arrivalTime = it.toSecondOfDay()
                entity.departureTime = it.toSecondOfDay()
            }
        }

        entity.stop = stop.toEntity(agency)
        entity.stopSequence = sequence

        return entity
    }

    override fun id(): String {
        throw OperationNotSupportedException("id() not necessary for stop time")
    }
}