package org.kkris.osmgtfs.osm.model

internal data class OsmRoute(
    val osmId: Long,
    val line: OsmLine,
    val sequence: List<OsmStop>,
    val path: List<Long>
)