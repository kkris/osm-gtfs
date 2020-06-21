package org.kkris.osmgtfs.agency

import mu.KotlinLogging
import org.kkris.osmgtfs.agency.matcher.RouteMatcher
import org.kkris.osmgtfs.agency.matcher.StopMatcher
import org.kkris.osmgtfs.common.Util
import org.kkris.osmgtfs.common.model.StopType
import org.kkris.osmgtfs.common.model.schedule.RegularSchedule
import org.kkris.osmgtfs.common.model.stop.Stop
import org.kkris.osmgtfs.common.model.timetable.Timetable
import org.kkris.osmgtfs.common.model.trip.Trip
import org.kkris.osmgtfs.common.model.trip.TripStop
import java.time.LocalTime
import java.util.Collections.max

// TODO: split up this class because it handles too much at once and is not easy to test
class TimetableComputer(private val stopMatcher: StopMatcher, private val routeMatcher: RouteMatcher) {

    private val logger = KotlinLogging.logger {}

    fun computeTimetable(schedule: RegularSchedule): Timetable? {
        val route = routeMatcher.match(schedule)

        if (route == null) {
            logger.warn { "Failed to match ${schedule.source} to a route" }
            return null
        }

        val origin = schedule.stops.first()
        val startTimes = Util.generateTimeseries(origin.first, origin.last, origin.intervals)

        val trips = startTimes.mapNotNull { begin ->
            var current = begin

            val stops = schedule.stops.mapNotNull { stop ->
                val time = Util.getSoonest(max(listOf(current, stop.first)), stop.last, stop.intervals)

                // in case no valid time is possible, this stop is invalid
                time ?: run {
                    logger.warn { "Found impossible stop time. Should be after ${max(listOf(current, stop.first))} but no later than ${stop.last} at a minute from ${stop.intervals}" }
                    return@mapNotNull null
                }

                current = time

                val matchedStop = stopMatcher.match(stop.name, route.stops) ?: run {
                    logger.error { "Failed to match stop with name '${stop.name}'"}
                    throw RuntimeException("Failed to find stop")
                }

                InternalTripStop(
                    matchedStop,
                    stop.type,
                    time
                )
            }

            // impossible trip, skip it
            if (stops.isEmpty()) {
                return@mapNotNull null
            }

            // merge stop times if two adjacent stops with the same name exist
            var mergedStops = stops.zipWithNext { prev, next ->
                if (prev.stop.name == next.stop.name && prev.type == StopType.ARRIVAL && next.type == StopType.DEPARTURE) {
                    TripStop(
                        prev.stop,
                        prev.time,
                        next.time
                    )
                } else {
                    TripStop(
                        prev.stop,
                        null,
                        prev.time
                    )
                }
            } + listOf(stops.last().let {
                TripStop(
                    it.stop,
                    null,
                    it.time
                )
            })

            mergedStops = mergedStops.filterIndexed { index, stop ->
                if (index > 0) {
                    val prev = mergedStops[index - 1]
                    prev.arrival == null
                } else {
                    true
                }
            }

            Trip(
                mergedStops,
                route,
                schedule.tripType
            )
        }

        return Timetable(
            schedule.operationType,
            trips
        )
    }
}

private data class InternalTripStop(
    val stop: Stop,
    val type: StopType,
    val time: LocalTime
)