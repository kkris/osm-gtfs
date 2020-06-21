package org.kkris.osmgtfs.common.model.timetable

import org.kkris.osmgtfs.common.model.ScheduleOperationType
import org.kkris.osmgtfs.common.model.trip.Trip

data class Timetable(
    val operationType: ScheduleOperationType,
    val trips: List<Trip>
)