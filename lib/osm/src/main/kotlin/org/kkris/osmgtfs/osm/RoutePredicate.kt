package org.kkris.osmgtfs.osm

interface RoutePredicate {
    fun matches(routeTag: String?, id: Long): Boolean
}