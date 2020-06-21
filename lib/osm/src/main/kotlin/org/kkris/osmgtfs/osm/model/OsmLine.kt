package org.kkris.osmgtfs.osm.model

internal data class OsmLine(
    val name: String,
    val origin: String,
    val destination: String,
    val color: String
)