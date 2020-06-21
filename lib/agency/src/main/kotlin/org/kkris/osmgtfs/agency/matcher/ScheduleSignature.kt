package org.kkris.osmgtfs.agency.matcher

import org.kkris.osmgtfs.common.model.ScheduleOperationType
import org.kkris.osmgtfs.common.model.TripOperationType

/**
 * Metadata about a schedule needed to identify it
 */
data class ScheduleSignature(
    val routeId: Long,
    val source: String,
    val scheduleType: ScheduleOperationType? = null,
    val tripType: TripOperationType? = null
)