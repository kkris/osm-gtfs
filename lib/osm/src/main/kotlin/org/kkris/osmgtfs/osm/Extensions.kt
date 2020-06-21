package org.kkris.osmgtfs.osm

import de.topobyte.osm4j.core.model.iface.OsmTag
import de.topobyte.osm4j.core.model.impl.Entity
import de.topobyte.osm4j.core.model.impl.Node
import de.topobyte.osm4j.core.model.impl.Way
import org.kkris.osmgtfs.common.Util
import org.kkris.osmgtfs.common.model.Coordinate
import kotlin.streams.asSequence


fun Entity.hasTag(key: String, value: String): Boolean {
    return tags.stream().asSequence()
        .any { it.key == key && it.value == value }
}

fun Entity.getTag(key: String): OsmTag? {
    return tags.stream().asSequence()
        .firstOrNull { it.key == key }
}

fun Entity.getTagValue(key: String, default: String = "unknown"): String {
    return tags.stream().asSequence()
        .firstOrNull { it.key == key }
        ?.value ?: default
}

fun Entity.getName(): String {
    return getTagValue("name", "unknown entity name")
}

fun Node.getCoordinate(): Coordinate {
    return Coordinate(latitude, longitude)
}

fun Way.getCentroid(): Coordinate {
    val cache = OsmEntityCache.get()
    val coordinates = nodes.toArray().toList()
        .mapNotNull { nodeId -> cache.getNode(nodeId) }
        .map { it.getCoordinate() }

    return Util.getCentroid(coordinates)
}