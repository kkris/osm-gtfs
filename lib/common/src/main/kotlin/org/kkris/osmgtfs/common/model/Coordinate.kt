package org.kkris.osmgtfs.common.model

import java.math.RoundingMode

data class Coordinate(
    val latitude: Double,
    val longitude: Double
) {
    override fun toString(): String {
        return "(${latitude.toBigDecimal().setScale(7, RoundingMode.HALF_DOWN)}, ${longitude.toBigDecimal().setScale(7, RoundingMode.HALF_DOWN)})"
    }
}