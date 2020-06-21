package org.kkris.osmgtfs.common.model.schedule

import org.kkris.osmgtfs.common.model.StopType
import java.time.LocalTime

data class ScheduleStop(
    val name: String,
    val type: StopType,
    val first: LocalTime,
    val last: LocalTime,
    val intervals: List<Int>, // TODO: naming
    val hasConstraint: Boolean? = null
)
