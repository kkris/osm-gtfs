package org.kkris.osmgtfs.agency

import io.kotest.core.spec.style.StringSpec
import io.kotest.inspectors.forAll
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.comparables.shouldBeGreaterThanOrEqualTo
import io.kotest.matchers.comparables.shouldBeLessThanOrEqualTo
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.arb
import io.kotest.property.checkAll
import org.kkris.osmgtfs.agency.matcher.RouteMatcher
import org.kkris.osmgtfs.agency.matcher.StopMatcher
import org.kkris.osmgtfs.common.model.Coordinate
import org.kkris.osmgtfs.common.model.ScheduleOperationType
import org.kkris.osmgtfs.common.model.StopType
import org.kkris.osmgtfs.common.model.route.Route
import org.kkris.osmgtfs.common.model.schedule.RegularSchedule
import org.kkris.osmgtfs.common.model.schedule.ScheduleStop
import org.kkris.osmgtfs.common.model.stop.Stop
import java.time.LocalTime
import kotlin.math.max
import kotlin.random.nextInt

class TimetableComputerTest: StringSpec({
    "test timetable generation" {
        checkAll(randomSchedule(includeMerging = false)) { schedule ->
            val computer = TimetableComputer(TestStopMatcher(), TestRouteMatcher())
            val timetable = computer.computeTimetable(schedule)

            // successfully generate timetable
            timetable.shouldNotBeNull()

            timetable.trips.forAll {
                // each stop in the schedule must be mapped to a stop in the trip
                it.stops.shouldHaveSize(schedule.stops.size)
                // computed stop times must be within bounds of schedule
                it.stops.zip(schedule.stops).forAll { (tripStop, scheduleStop) ->
                    tripStop.departure.shouldBeGreaterThanOrEqualTo(scheduleStop.first)
                    tripStop.departure.shouldBeLessThanOrEqualTo(scheduleStop.last)
                    tripStop.arrival?.shouldBeGreaterThanOrEqualTo(scheduleStop.first)
                    tripStop.arrival?.shouldBeLessThanOrEqualTo(scheduleStop.last)
                    tripStop.stop.name.shouldBe(scheduleStop.name)
                }
                // stop times must be monotonic
                it.stops.zipWithNext { prev, current ->
                    prev.arrival?.shouldBeLessThanOrEqualTo(current.departure)
                    prev.departure.shouldBeLessThanOrEqualTo(current.departure)
                    current.arrival?.let { arrival -> prev.departure.shouldBeLessThanOrEqualTo(arrival) }
                    current.arrival?.let { arrival -> prev.arrival?.shouldBeLessThanOrEqualTo(arrival) }
                }
            }
        }
    }
})

private fun randomSchedule(includeMerging: Boolean): Arb<RegularSchedule> {
    return arb { rs ->
        generateSequence {
            val length = rs.random.nextInt(0..4)
            val start = LocalTime.of(0, 1, 0)
            val end = LocalTime.of(9, 0, 0)

            var offsetMinutes = 0L
            val intervalLength = rs.random.nextInt(1..3)

            val stops = (0..length).map { index ->
                val type = StopType.values()[rs.random.nextInt(StopType.values().indices)]
                val start = start.plusMinutes(offsetMinutes)
                val end = end.plusMinutes(offsetMinutes)

                val intervals = ((0 until intervalLength).map { _ ->
                    (offsetMinutes.toInt() + rs.random.nextInt(0..60)) % 60
                } + listOf(end.minute)).sorted().distinct()
                offsetMinutes += rs.random.nextInt(1..120)
                val stopIndex = if (includeMerging) {
                    max(0, index - rs.random.nextInt(0..1))
                } else {
                    index
                }

                ScheduleStop(
                    "stop-$stopIndex",
                    type,
                    start,
                    end,
                    intervals

                )
            }

            RegularSchedule(
                ScheduleOperationType.values()[rs.random.nextInt(ScheduleOperationType.values().indices)],
                stops
            )

        }
    }
}

private class TestStopMatcher: StopMatcher {
    override fun match(stopName: String, stops: List<Stop>): Stop? {
        return Stop(null, stopName, Coordinate(0.0, 0.0), null, null)
    }
}

private class TestRouteMatcher: RouteMatcher(emptyList(), emptyList()) {
    override fun match(schedule: RegularSchedule): Route? {
        // just return dummy route
        return Route(
            1L,
            "1",
            "",
            emptyList(),
            emptyList()
        )
    }
}