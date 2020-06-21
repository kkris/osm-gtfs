package org.kkris.osmgtfs.common.model.schedule

import org.kkris.osmgtfs.common.model.ScheduleOperationType
import org.kkris.osmgtfs.common.model.TripOperationType
import org.kkris.osmgtfs.common.model.TripOperationType.REGULAR

/**
 * Represents a schedule where a list of stops is visited in regular intervals
 */
data class RegularSchedule(
    val operationType: ScheduleOperationType,
    val stops: List<ScheduleStop>,
    val tripType: TripOperationType = REGULAR,
    val source: String? = null // generic tag to indicate the source of this schedule. this can be used to later match this with additional data
)