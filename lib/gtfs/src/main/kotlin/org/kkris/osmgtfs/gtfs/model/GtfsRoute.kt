package org.kkris.osmgtfs.gtfs.model

import org.kkris.osmgtfs.common.identifier
import org.onebusaway.gtfs.model.AgencyAndId
import org.onebusaway.gtfs.model.Route

data class GtfsRoute(
    val name: String,
    val color: String
): GtfsEntity<Route>() {
    companion object {
        const val ROUTE_TYPE_BUS = 3
        const val COLOR_WHITE = "FFFFFF"
    }

    override fun toEntity(agency: GtfsAgency): Route {
        val entity = Route()

        entity.id = AgencyAndId(agency.id, id())
        entity.shortName = name
        entity.type = ROUTE_TYPE_BUS
        entity.color = color
        entity.textColor = COLOR_WHITE

        return entity
    }

    override fun id(): String {
        return "route:$name".identifier()
    }
}