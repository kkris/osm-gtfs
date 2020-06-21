package org.kkris.osmgtfs.agency

import org.kkris.osmgtfs.agency.matcher.RouteMatcher
import org.kkris.osmgtfs.agency.matcher.StopMatcher
import org.kkris.osmgtfs.common.model.fare.Fare
import org.kkris.osmgtfs.common.model.route.Route
import org.kkris.osmgtfs.common.serialization.ScheduleSerializer
import org.kkris.osmgtfs.gtfs.model.GtfsAgency
import org.kkris.osmgtfs.gtfs.model.GtfsFeedInfo
import java.time.LocalDate

// TODO: naming
abstract class AbstractAgency: RouteProvider, ScheduleProvider {

    abstract fun getAgency(): GtfsAgency
    abstract fun getFares(): List<Fare>

    // feed metadata hooks
    abstract fun getFeedInfo(): GtfsFeedInfo
    abstract fun getFeedValidity(): Pair<LocalDate, LocalDate>

    abstract fun getStopMatcher(): StopMatcher
    abstract fun getRouteMatcher(routes: List<Route>): RouteMatcher

    fun generate(outputDirectory: String) {
        // extract routes from OSM
        val routes = getRoutes()

        // get list of schedules
        val schedules = getSchedules()

        // serialize and save schedules (for manual verification)
        ScheduleSerializer.save("$outputDirectory/schedule.json", schedules)

        // compute a complete timetable
        // in the process, the schedule is mapped to the OSM route including the correct stop name
        val computer = TimetableComputer(getStopMatcher(), getRouteMatcher(routes.routes))
        val timetables = schedules.schedules.mapNotNull { computer.computeTimetable(it) }

        val handler = GtfsHandler(getAgency(), routes.stops, routes.routes, timetables, getFares(), getFeedInfo(), getFeedValidity())
        handler.write(outputDirectory)
    }
}