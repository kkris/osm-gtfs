package org.kkris.osmgtfs.osm.model

import de.topobyte.osm4j.core.model.iface.EntityType

internal data class OsmPlatform(
    val osmId: Long,
    val type: EntityType
)