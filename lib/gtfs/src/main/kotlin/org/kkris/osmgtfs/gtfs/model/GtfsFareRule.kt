package org.kkris.osmgtfs.gtfs.model

import org.onebusaway.gtfs.model.FareRule

data class GtfsFareRule(
    val fare: GtfsFare,
    val route: GtfsRoute
): GtfsEntity<FareRule>() {

    override fun toEntity(agency: GtfsAgency): FareRule {
        val entity = FareRule()

        entity.fare = fare.toEntity(agency)
        entity.route = route.toEntity(agency)

        return entity
    }

    override fun id(): String {
        throw UnsupportedOperationException("id() not needed for fare rules")
    }
}