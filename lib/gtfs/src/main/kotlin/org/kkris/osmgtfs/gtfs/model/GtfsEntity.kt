package org.kkris.osmgtfs.gtfs.model

abstract class GtfsEntity<E> {
    abstract fun toEntity(agency: GtfsAgency): E
    abstract fun id(): String
}