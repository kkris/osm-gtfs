package org.kkris.osmgtfs.common.model

import org.kkris.osmgtfs.common.model.route.Route
import org.kkris.osmgtfs.common.model.stop.Stop

data class RouteData( // TODO: naming
    val stops: List<Stop>,
    val routes: List<Route>
)