package org.kkris.osmgtfs.gtfs.model

import org.kkris.osmgtfs.common.identifier
import org.kkris.osmgtfs.common.model.Coordinate
import org.kkris.osmgtfs.gtfs.util.DeterministicIdGenerator
import org.onebusaway.gtfs.model.AgencyAndId
import org.onebusaway.gtfs.model.Stop

data class GtfsStop(
    val name: String,
    val coordinate: Coordinate,
    val parent: GtfsStop?,
    val platformName: String?
) : GtfsEntity<Stop>() {

    companion object {
        private val idGenerator = DeterministicIdGenerator()
    }

    override fun toEntity(agency: GtfsAgency): Stop {
        val entity = Stop()

        entity.id = AgencyAndId(agency.id, id())
        entity.name = name
        entity.locationType = when (parent) {
            null -> 1 // this stop is a parent station
            else -> 0 // this stop is a platform
        }
        parent?.let {
            entity.parentStation = it.id()
        }
        platformName?.let {
            entity.platformCode = it
        }
        entity.lat = coordinate.latitude
        entity.lon = coordinate.longitude

        return entity
    }

    override fun id(): String {
        // no two stops have the same coordinate, so it can be used to derive a stable id
        // a station which only has one child stop will have the same coordinate as it's child
        // but they will get a different prefix below so it does not matter in the context of the GTFS feed
        val id = idGenerator.getId(coordinate)
        return when (parent) {
            null -> "station:$id".identifier()
            else -> "stop:$id".identifier()
        }
    }
}