package org.kkris.osmgtfs.common.model.stop

import org.kkris.osmgtfs.common.model.Coordinate


data class Stop(
    val osmId: Long?,
    val name: String,
    val coordinate: Coordinate,
    val parentStop: Stop?,
    val platformName: String?
)