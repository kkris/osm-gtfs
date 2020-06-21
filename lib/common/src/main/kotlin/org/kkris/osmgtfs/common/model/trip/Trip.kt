package org.kkris.osmgtfs.common.model.trip

import org.kkris.osmgtfs.common.model.TripOperationType
import org.kkris.osmgtfs.common.model.route.Route

data class Trip(
    val stops: List<TripStop>,
    val route: Route,
    val type: TripOperationType? = null
)