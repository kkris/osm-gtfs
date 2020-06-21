package org.kkris.osmgtfs.agency

import org.kkris.osmgtfs.common.model.ScheduleOperationType
import org.kkris.osmgtfs.common.model.fare.Fare
import org.kkris.osmgtfs.common.model.route.Route
import org.kkris.osmgtfs.common.model.stop.Stop
import org.kkris.osmgtfs.common.model.timetable.Timetable
import org.kkris.osmgtfs.gtfs.GtfsFileWriter
import org.kkris.osmgtfs.gtfs.GtfsMapper
import org.kkris.osmgtfs.gtfs.model.GtfsAgency
import org.kkris.osmgtfs.gtfs.model.GtfsCalendar
import org.kkris.osmgtfs.gtfs.model.GtfsCalendarDate
import org.kkris.osmgtfs.gtfs.model.GtfsFeedInfo
import java.time.DayOfWeek
import java.time.LocalDate
import java.util.*

// TODO: naming
class GtfsHandler(
    private val agency: GtfsAgency,
    private val stops: List<Stop>,
    private val routes: List<Route>,
    private val timetables: List<Timetable>,
    private val fares: List<Fare>,
    private val feedInfo: GtfsFeedInfo,
    private val feedValidity: Pair<LocalDate, LocalDate>
) {
    fun write(outputDirectory: String) {
        // generate GTFS feed
        val writer = GtfsFileWriter("$outputDirectory/gtfs")

        val stops = GtfsMapper.mapStops(stops)
        val routes = GtfsMapper.mapRoutes(routes)
        val trips = GtfsMapper.mapTrips(timetables, feedValidity)
        val fares = GtfsMapper.mapFares(fares)

        writer.write(
            agency = agency,
            stops = stops,
            routes = routes,
            trips = trips,
            stopTimes = GtfsMapper.mapStopTimes(trips),
            calendars = getCalendars(),
            calendarDates = getServiceExceptions(),
            shapes = GtfsMapper.mapShapes(trips),
            fares = fares,
            fareRules = GtfsMapper.mapFareRules(routes, fares),
            feedInfo = feedInfo
        )
    }

    /**
     * Return a list of calendars derived from which schedule types are present
     */
    private fun getCalendars(): List<GtfsCalendar> {
        // query all schedules and gather their schedule type
        return timetables
            .map {
                it.operationType
            }
            .distinct()
            .map {
                GtfsCalendar(it, feedValidity)
            }
    }

    /**
     * Return a list of service exceptions derived from local holidays
     */
    private fun getServiceExceptions(): List<GtfsCalendarDate> {
        val locale = Locale("de", "AT") // TODO: make configurable

        return Util.getHolidays(locale, feedValidity.first, feedValidity.second).flatMap { date ->
            val disabledService = if (date.dayOfWeek == DayOfWeek.SATURDAY) {
                GtfsCalendarDate(
                    GtfsCalendar(ScheduleOperationType.SATURDAY, feedValidity),
                    date,
                    GtfsCalendarDate.ExceptionType.REMOVED
                )
            } else {
                GtfsCalendarDate(
                    GtfsCalendar(ScheduleOperationType.WEEKDAY, feedValidity),
                    date,
                    GtfsCalendarDate.ExceptionType.REMOVED
                )
            }

            listOf(
                // disable normal service
                disabledService,
                // enable holiday service
                GtfsCalendarDate(
                    GtfsCalendar(ScheduleOperationType.SUNDAY_HOLIDAY, feedValidity),
                    date,
                    GtfsCalendarDate.ExceptionType.ADDED
                )
            )
        }
    }
}