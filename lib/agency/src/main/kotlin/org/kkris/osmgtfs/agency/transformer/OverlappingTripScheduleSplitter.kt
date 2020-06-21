package org.kkris.osmgtfs.agency.transformer

import org.kkris.osmgtfs.common.Util
import org.kkris.osmgtfs.common.model.TripOperationType
import org.kkris.osmgtfs.common.model.TripOperationType.REGULAR
import org.kkris.osmgtfs.common.model.TripOperationType.SHORT
import org.kkris.osmgtfs.common.model.schedule.RegularSchedule
import org.kkris.osmgtfs.common.model.schedule.ScheduleStop

/**
 * Schedule splitter which splits a schedule which has a prefix which operates longer than the suffix.
 */
class OverlappingTripScheduleSplitter: ScheduleSplitter() {

    override fun split(schedule: RegularSchedule): List<RegularSchedule> {
        var types = listOf(REGULAR, SHORT)
        val annotatedStops = schedule.stops.zipWithNext { current, next ->
            val annotated =  Pair(current, types)
            if (current.last > next.last) {
                types = listOf(REGULAR)
            }

            annotated
        }.toMutableList()

        annotatedStops.add(Pair(schedule.stops.last(), annotatedStops.last().second))

        val lastOperationOfRegularSchedule = annotatedStops.zipWithNext { current, prev ->
            if (current.second != prev.second) {
                prev.first.last
            } else null
        }.filterNotNull().first()

        val regularTripStops = annotatedStops
            .filter { (_, types) ->
                types.contains(REGULAR)
            }
            .map { (stop, types) ->
                if (types.contains(SHORT)) {
                    stop.copy(last = lastOperationOfRegularSchedule)
                } else stop
            }

        val shortTripStops = annotatedStops
            .filter { (_, types) ->
                types.contains(SHORT)
            }
            .map { (stop, _) ->
                // short schedule replaces the regular schedule at some point. must not start before
                val notBefore = Util.getSoonest(lastOperationOfRegularSchedule, stop.last, stop.intervals)!!
                stop.copy(first = notBefore)
            }

        return listOf(
            schedule.copy(
                stops = regularTripStops,
                tripType = REGULAR
            ),
            schedule.copy(
                stops = shortTripStops,
                tripType = SHORT
            )
        )
    }

    override fun getTripTypesForStop(stop: ScheduleStop): List<TripOperationType> {
        throw IllegalStateException("Not needed")
    }

    override fun transform(stop: ScheduleStop, type: TripOperationType): ScheduleStop {
        throw IllegalStateException("Not needed")
    }
}