package org.kkris.osmgtfs.agency.transformer

import org.kkris.osmgtfs.common.model.TripOperationType
import org.kkris.osmgtfs.common.model.TripOperationType.REGULAR
import org.kkris.osmgtfs.common.model.TripOperationType.SHORT
import org.kkris.osmgtfs.common.model.schedule.RegularSchedule
import org.kkris.osmgtfs.common.model.schedule.ScheduleStop

abstract class ScheduleSplitter {

    protected abstract fun getTripTypesForStop(stop: ScheduleStop): List<TripOperationType>
    protected abstract fun transform(stop: ScheduleStop, type: TripOperationType): ScheduleStop

    open fun split(schedule: RegularSchedule): List<RegularSchedule> {
        val regularTripStops = schedule.stops
            .filter { stop ->
                getTripTypesForStop(stop).contains(REGULAR)
            }
            .map { stop ->
                transform(stop, REGULAR)
            }
        val shortTripStops = schedule.stops
            .filter { stop ->
                getTripTypesForStop(stop).contains(SHORT)
            }
            .map { stop ->
                transform(stop, SHORT)
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
}