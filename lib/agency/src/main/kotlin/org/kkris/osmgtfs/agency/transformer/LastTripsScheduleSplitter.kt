package org.kkris.osmgtfs.agency.transformer

import org.kkris.osmgtfs.common.model.TripOperationType
import org.kkris.osmgtfs.common.model.TripOperationType.REGULAR
import org.kkris.osmgtfs.common.model.TripOperationType.SHORT
import org.kkris.osmgtfs.common.model.schedule.ScheduleStop

/**
 * Schedule splitter which splits of the last n trips of a schedule which are operated as short trips
 */
class LastTripsScheduleSplitter(private val n: Int): ScheduleSplitter() {

    override fun getTripTypesForStop(stop: ScheduleStop): List<TripOperationType> {
        return when (stop.hasConstraint) {
            true -> listOf(REGULAR, SHORT)
            else -> listOf(REGULAR)
        }
    }

    override fun transform(stop: ScheduleStop, type: TripOperationType): ScheduleStop {
        return when (type) {
            REGULAR -> {
                // cut off the last n trips from the regular schedule by reducing the service period by an appropriate offset
                // for the regular schedule for stops on the short route. This way, the last trips won't be generated because
                // they fall outside of the operating window
                when (getTripTypesForStop(stop).contains(SHORT)) {
                    true -> stop.copy(last = stop.last.minusMinutes(getOffset()))
                    else -> stop
                }
            }
            SHORT -> {
                // restrict short trip stops to only the last n trips by setting the first trip time the trip can be scheduled to
                // an appropriate time n-times before the end of schedule
                stop.copy(first = stop.last.minusMinutes(getOffset()))
            }
        }
    }

    /**
     * Computes the needed offset to cut of the last n trips
     * Note: this assumes that frequency is no more than twice per hour and intervals are even
     * TODO: improve
     */
    private fun getOffset(): Long {
        return (n - 1) * 30L + 1
    }
}