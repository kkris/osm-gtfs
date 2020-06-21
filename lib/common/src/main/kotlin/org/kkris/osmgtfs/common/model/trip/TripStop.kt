package org.kkris.osmgtfs.common.model.trip

import org.kkris.osmgtfs.common.model.stop.Stop
import java.time.LocalTime

data class TripStop(
    val stop: Stop,
    val arrival: LocalTime?,
    val departure: LocalTime
)
