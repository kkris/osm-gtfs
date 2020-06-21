package org.kkris.osmgtfs.agency.matcher

import org.kkris.osmgtfs.common.model.route.Route
import org.kkris.osmgtfs.common.model.schedule.RegularSchedule

/**
 * Matches a given schedule (by it's signature) to a route
 */
open class RouteMatcher(private val signatures: List<ScheduleSignature>, private val routes: List<Route>) {
    open fun match(schedule: RegularSchedule): Route? {
        val routeId = signatures.firstOrNull { type ->
            if (schedule.source != type.source) return@firstOrNull false
            type.scheduleType?.let {
                if (it != schedule.operationType) return@firstOrNull false
            }
            type.tripType?.let {
                if (it != schedule.tripType) return@firstOrNull false
            }

            true
        }?.routeId ?: return null

        return routes.firstOrNull { it.id == routeId }
    }
}