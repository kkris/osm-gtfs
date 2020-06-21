package org.kkris.osmgtfs.agency.transformer

import org.kkris.osmgtfs.common.model.TripOperationType
import org.kkris.osmgtfs.common.model.TripOperationType.REGULAR
import org.kkris.osmgtfs.common.model.TripOperationType.SHORT
import org.kkris.osmgtfs.common.model.schedule.ScheduleStop

/**
 * Schedule splitter which splits a schedule which has two different origins. This is the case when parts of the
 * regular schedule is serviced more frequently by a short route sharing part of the trip.
 */
class MergingTripScheduleSplitter(
    private val intervalTransformer: (ScheduleStop, TripOperationType) -> List<Int>
): ScheduleSplitter() {

    override fun getTripTypesForStop(stop: ScheduleStop): List<TripOperationType> {
        return when (stop.intervals.size) {
            1 -> listOf(REGULAR)
            else -> listOf(REGULAR, SHORT)
        }
    }

    override fun transform(stop: ScheduleStop, type: TripOperationType): ScheduleStop {
        return stop.copy(intervals = intervalTransformer(stop, type))
    }
}