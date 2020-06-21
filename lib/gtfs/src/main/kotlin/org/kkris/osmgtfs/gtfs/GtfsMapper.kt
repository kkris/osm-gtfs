package org.kkris.osmgtfs.gtfs

import org.kkris.osmgtfs.common.model.fare.Fare
import org.kkris.osmgtfs.common.model.route.Route
import org.kkris.osmgtfs.common.model.stop.Stop
import org.kkris.osmgtfs.common.model.timetable.Timetable
import org.kkris.osmgtfs.gtfs.model.*
import java.time.LocalDate

object GtfsMapper {

    fun mapStops(stops: List<Stop>): List<GtfsStop> {
        return stops
            .map(::mapStop)
            .distinct()
            .sortedBy { "${it.name}-${it.coordinate}" }
    }

    fun mapRoutes(routes: List<Route>): List<GtfsRoute> {
        return routes
            .map {
                GtfsRoute(
                    it.name,
                    it.color
                )
            }
            .distinctBy { it.id() }
            .sortedBy { it.id() }
    }

    fun mapTrips(timetables: List<Timetable>, feedValidity: Pair<LocalDate, LocalDate>): List<GtfsTrip> {
        return timetables.flatMap { timetable ->
            var tripNumber = 0
            timetable.trips.map { trip ->
                GtfsTrip(
                    GtfsRoute(
                        trip.route.name,
                        trip.route.color
                    ),
                    trip.route.origin(),
                    trip.route.destination(),
                    trip.stops,
                    trip.route.path,
                    GtfsCalendar(timetable.operationType, feedValidity),
                    tripNumber++,
                    trip.type
                )
            }
        }
    }

    fun mapStopTimes(trips: List<GtfsTrip>): List<GtfsStopTime> {
        return trips.flatMap { trip ->
            trip.stops.mapIndexed { sequence, stop ->
                GtfsStopTime(
                    trip,
                    mapStop(stop.stop),
                    stop.departure,
                    stop.arrival,
                    sequence
                )
            }
        }
    }

    fun mapShapes(trips: List<GtfsTrip>): List<GtfsShapePoint> {
        return trips
            .flatMap {
                it.getShape()
            }
            .distinctBy { "${it.shapeId}-${it.sequence}" } // de-duplicate shapes
    }

    fun mapFares(fares: List<Fare>): List<GtfsFare> {
        return fares.map {
            GtfsFare(
                it.id,
                it.price,
                it.currency,
                it.paymentMethod
            )
        }
    }

    fun mapFareRules(routes: List<GtfsRoute>, fares: List<GtfsFare>): List<GtfsFareRule> {
        return routes.flatMap { route ->
            fares.map {  fare ->
                GtfsFareRule(fare, route)
            }
        }
    }
}

private fun mapStop(stop: Stop): GtfsStop {
    return GtfsStop(
        stop.name,
        stop.coordinate,
        stop.parentStop?.let { mapStop(it) },
        stop.platformName
    )
}