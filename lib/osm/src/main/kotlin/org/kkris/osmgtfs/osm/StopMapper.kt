package org.kkris.osmgtfs.osm

import de.topobyte.osm4j.core.model.iface.EntityType
import mu.KotlinLogging
import org.kkris.osmgtfs.common.Util
import org.kkris.osmgtfs.common.model.Coordinate
import org.kkris.osmgtfs.common.model.stop.Stop
import org.kkris.osmgtfs.osm.model.OsmPlatform
import org.kkris.osmgtfs.osm.model.OsmRoute
import org.kkris.osmgtfs.osm.model.OsmStop

internal object StopMapper {

    private val logger = KotlinLogging.logger {}

    fun mapStops(routes: List<OsmRoute>): List<Stop> {
        // get a list of all stops
        val osmStops = routes
            .flatMap { route ->
                route.sequence
            }
            .distinctBy { it.osmId }

        // map stops to internal model
        // this can produce multiple stops with the same name (but different coordinates or platformNames
        // (e.g. stops on different sides of the same road near each other)
        val stops = osmStops
            .mapNotNull { stop ->
                getOsmStopData(stop)
            }
            .map { stop ->
                Stop(
                    stop.osmId,
                    stop.name,
                    stop.coordinate,
                    null,
                    stop.platformName
                )
            }

        // find stops with the same name and group them by a new parent stop
        return stops
            .groupBy { it.name }
            .flatMap { (name, similarStops) ->
                logger.debug {
                    val repr = similarStops.joinToString(separator = ", ") {
                        it.platformName?.let { platform ->
                            "$platform: ${it.coordinate}"
                        } ?: "${it.coordinate}"
                    }
                    "Grouping stops named '$name' together: $repr"
                }

                val parentCoordinate = Util.getCentroid(similarStops.map { it.coordinate })
                val parentStop = Stop(
                    null,
                    name,
                    parentCoordinate,
                    null,
                    null
                )

                // return parent stop and modified child stops where parentStop is set
                listOf(parentStop) + similarStops.map { it.copy(parentStop = parentStop) }
            }
    }

    private fun getOsmStopData(stop: OsmStop): StopData? {
        val cache = OsmEntityCache.get()

        return cache.getNode(stop.osmId)?.let { node ->
            getOsmPlatformData(stop.platform)?.let { (platformName, platformCoordinate) ->
                StopData(
                    stop.osmId,
                    node.getName(),
                    // Note: we could use node.getCoordinate() here as well, but the platform is a more precise regarding
                    // the actual boarding location
                    platformCoordinate,
                    // only use platform name if it is not just duplicating the stop name
                    if (node.getName() == platformName) null else platformName
                )
            }
        }
    }

    private fun getOsmPlatformData(platform: OsmPlatform): Pair<String, Coordinate>? {
        val cache = OsmEntityCache.get()

        return when (platform.type) {
            EntityType.Node -> {
                cache.getNode(platform.osmId)?.let { node ->
                    Pair(node.getName(), node.getCoordinate())
                }
            }
            EntityType.Way -> {
                cache.getWay(platform.osmId)?.let { way ->
                    Pair(way.getName(), way.getCentroid())
                }
            }
            else -> null
        }
    }
}

private data class StopData(
    val osmId: Long,
    val name: String,
    val coordinate: Coordinate,
    val platformName: String?
)