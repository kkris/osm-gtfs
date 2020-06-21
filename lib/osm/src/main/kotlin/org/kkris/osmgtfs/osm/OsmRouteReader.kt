package org.kkris.osmgtfs.osm

import org.kkris.osmgtfs.common.model.RouteData
import org.kkris.osmgtfs.osm.route.OsmRouteExtractor

class OsmRouteReader(private val pbfPath: String) {

    fun read(filter: RoutePredicate): RouteData {
        // initialize entity cache
        OsmEntityCache.init(pbfPath)

        // read matching routes from pbf
        val extractor = OsmRouteExtractor(pbfPath)
        val osmRoutes = extractor.extract(filter)

        // map to common model
        val stops = StopMapper.mapStops(osmRoutes)
        val routes = osmRoutes.map { RouteMapper.mapRoute(it, stops) }

        return RouteData(
            stops,
            routes
        )
    }
}