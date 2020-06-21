package org.kkris.osmgtfs.osm.route

import de.topobyte.osm4j.core.model.iface.EntityType
import org.kkris.osmgtfs.osm.RoutePredicate
import org.kkris.osmgtfs.osm.getTag
import org.kkris.osmgtfs.osm.getTagValue
import org.kkris.osmgtfs.osm.hasTag
import org.kkris.osmgtfs.osm.model.OsmLine
import org.kkris.osmgtfs.osm.model.OsmPlatform
import org.kkris.osmgtfs.osm.model.OsmRoute
import org.kkris.osmgtfs.osm.model.OsmStop
import org.kkris.osmgtfs.osm.reader.OsmRelationReader

internal class OsmRouteExtractor(private val pbfPath: String) {

    fun extract(filter: RoutePredicate): List<OsmRoute> {
        val reader = OsmRelationReader(pbfPath) {
            it.hasTag("type", "route") && filter.matches(it.getTag("route")?.value, it.id)
        }

        return reader
            .read()
            .map { relation ->
                val stops = relation.members.filter { it.role == "stop" }
                val platforms = relation.members.filter { it.role == "platform" }
                val path = relation.members
                    .filter { it.type == EntityType.Way }
                    .map { it.id }
                    .drop(1) // drop first because it does not really mark the start point (TODO: check if this applies to every route)

                if (stops.size != platforms.size) {
                    throw RuntimeException("ups..")
                }

                // match stops with their platforms
                val stopSequence = stops.zip(platforms)
                    .map { (stop, platform) ->
                        OsmStop(stop.id, OsmPlatform(platform.id, platform.type))
                    }

                val line = OsmLine(
                    relation.getTagValue("ref"),
                    relation.getTagValue("from"),
                    relation.getTagValue("to"),
                    relation.getTagValue("colour").replace("#", "")
                )

                OsmRoute(
                    relation.id,
                    line,
                    stopSequence,
                    path
                )
            }
            .distinctBy { it.osmId }
    }
}