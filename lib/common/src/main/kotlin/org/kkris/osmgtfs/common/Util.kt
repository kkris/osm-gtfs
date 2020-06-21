package org.kkris.osmgtfs.common

import org.kkris.osmgtfs.common.model.Coordinate
import java.time.LocalTime
import kotlin.math.*

object Util {

    fun distance(first: Coordinate, second: Coordinate): Double {
        val R = 6371 // Radius of the earth
        val latDistance = Math.toRadians(second.latitude - first.latitude)
        val lonDistance = Math.toRadians(second.longitude - first.longitude)
        val a = (sin(latDistance / 2) * sin(latDistance / 2)
            + (cos(Math.toRadians(first.latitude)) * cos(Math.toRadians(second.latitude))
            * sin(lonDistance / 2) * sin(lonDistance / 2)))
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        var distance = R * c * 1000 // convert to meters
        distance = distance.pow(2.0)
        return sqrt(distance)
    }

    fun generateTimeseries(begin: LocalTime, end: LocalTime, intervals: List<Int>): List<LocalTime> {
        val times = mutableListOf<LocalTime>()
        var current = begin
        while (current <= end) {
            intervals.forEach { minute ->
                current = current.withMinute(minute)
                if (current in begin..end) {
                    times.add(current)
                }
            }

            current = current.plusHours(1).withMinute(0)
        }

        return times
            .distinct()
            .sorted()
    }

    fun getSoonest(after: LocalTime, latest: LocalTime, intervals: List<Int>): LocalTime? {
        val candidates = intervals
            .flatMap { listOf(after.withMinute(it), after.plusHours(1).withMinute(it)) }
            .filter { it == after || it == latest || (it.isAfter(after) && it.isBefore(latest)) }
            .distinct()
            .sorted()

        return candidates.firstOrNull()
    }

    fun getCentroid(coordinates: List<Coordinate>): Coordinate {
        return coordinates
            .reduce { acc, coordinate ->
                Coordinate(acc.latitude + coordinate.latitude, acc.longitude + coordinate.longitude)
            }.let {
                Coordinate(it.latitude / coordinates.size, it.longitude / coordinates.size)
            }
    }
}