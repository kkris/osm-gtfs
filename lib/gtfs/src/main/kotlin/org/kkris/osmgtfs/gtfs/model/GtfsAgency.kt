package org.kkris.osmgtfs.gtfs.model

import org.onebusaway.gtfs.model.Agency

data class GtfsAgency(
    val id: String,
    val name: String,
    val url: String,
    val timezone: String,
    val language: String
) {

    fun toEntity(): Agency {
        val entity = Agency()

        entity.id = id
        entity.name = name
        entity.url = url
        entity.timezone = timezone
        entity.lang = language

        return entity
    }
}