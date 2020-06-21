package org.kkris.osmgtfs.lup

import org.kkris.osmgtfs.agency.AbstractAgency
import org.kkris.osmgtfs.agency.matcher.RouteMatcher
import org.kkris.osmgtfs.agency.matcher.ScheduleSignature
import org.kkris.osmgtfs.agency.matcher.StopMatcher
import org.kkris.osmgtfs.agency.transformer.LastTripsScheduleSplitter
import org.kkris.osmgtfs.agency.transformer.MergingTripScheduleSplitter
import org.kkris.osmgtfs.agency.transformer.OverlappingTripScheduleSplitter
import org.kkris.osmgtfs.common.model.RouteData
import org.kkris.osmgtfs.common.model.ScheduleOperationType.*
import org.kkris.osmgtfs.common.model.TripOperationType.REGULAR
import org.kkris.osmgtfs.common.model.TripOperationType.SHORT
import org.kkris.osmgtfs.common.model.fare.Fare
import org.kkris.osmgtfs.common.model.fare.PaymentMethod
import org.kkris.osmgtfs.common.model.route.Route
import org.kkris.osmgtfs.common.model.schedule.RegularSchedule
import org.kkris.osmgtfs.common.model.schedule.Schedules
import org.kkris.osmgtfs.gtfs.model.GtfsAgency
import org.kkris.osmgtfs.gtfs.model.GtfsFeedInfo
import org.kkris.osmgtfs.lup.pdf.PdfPlanParser
import org.kkris.osmgtfs.lup.pdf.model.PageDescriptor
import org.kkris.osmgtfs.lup.pdf.model.PageSection
import org.kkris.osmgtfs.lup.pdf.model.PageType
import org.kkris.osmgtfs.osm.OsmRouteReader
import org.kkris.osmgtfs.osm.RoutePredicate
import java.io.FileInputStream
import java.time.LocalDate
import java.util.*
import java.util.Collections.max
import java.util.Collections.min

class LupGtfsGenerator(
    private val schedulePath: String,
    private val osmPath: String,
    private val startDate: LocalDate,
    private val endDate: LocalDate
): AbstractAgency() {
    companion object {

        private val PAGE_DESCRIPTORS = listOf(
            PageDescriptor(2, listOf(PageSection())),
            // TODO: handle 1 being continued as 5 from traisenpark
            // TODO: sunday
            PageDescriptor(3, listOf(PageSection(columns = listOf(WEEKDAY, SATURDAY), bottomOffset = 25))),
            PageDescriptor(4, listOf(PageSection(bottomOffset = 25))),
            PageDescriptor(5, listOf(PageSection())),
            PageDescriptor(6, listOf(PageSection())),
            PageDescriptor(7, listOf(PageSection(bottomOffset = 25))),
            PageDescriptor(8, listOf(PageSection())),
            PageDescriptor(9, listOf(PageSection())),
            PageDescriptor(10, listOf(PageSection(bottomOffset = 25))),
            PageDescriptor(11, listOf(PageSection())),
            PageDescriptor(12, listOf(PageSection(bottomOffset = 25))),
            PageDescriptor(13, listOf(PageSection())),
            PageDescriptor(15, listOf(PageSection(columns = listOf(COMMUTER_WEEKDAY, WEEKDAY, COMMUTER_SATURDAY, SATURDAY, SUNDAY_HOLIDAY)))),
            PageDescriptor(16, listOf(PageSection(bottomOffset = 25))),
            PageDescriptor(17, listOf(PageSection(columns = listOf(WEEKDAY, SATURDAY)))),
            PageDescriptor(18, listOf(PageSection(columns = listOf(WEEKDAY, SATURDAY)))),
            PageDescriptor(19, listOf(PageSection(columns = listOf(SUNDAY_HOLIDAY)))),
            PageDescriptor(20, listOf(PageSection(columns = listOf(SUNDAY_HOLIDAY)))),
            PageDescriptor(21, listOf(
                PageSection(PageType.UPPER_HALF),
                PageSection(PageType.LOWER_HALF)
            )),
            // TODO: page 22
            // TODO: page 23
            PageDescriptor(24, listOf(
                PageSection(PageType.UPPER_HALF, columns = listOf(WEEKDAY)),
                PageSection(PageType.LOWER_HALF, columns = listOf(WEEKDAY))
            )),
            PageDescriptor(25, listOf(
                PageSection(PageType.UPPER_HALF, columns = listOf(WEEKDAY)),
                PageSection(PageType.LOWER_HALF, columns = listOf(WEEKDAY))
            ))
        )

        private val SCHEDULES = listOf(
            // LUP 1: Hart bei St. Pölten Ghegastraße => Viehofen Living City
            ScheduleSignature(8845052L, "page:2"),
            // LUP 1: Viehofen Living City => Hart bei St. Pölten Ghegastraße
            ScheduleSignature(8845055L, "page:3"),
            // LUP 2: Harland Amtshaus => St. Pölten Gartenstadt Kremserberg (Sonntag)
            ScheduleSignature(9063949L, "page:4", scheduleType = SUNDAY_HOLIDAY),
            // LUP 2: Harland Amtshaus => St. Pölten Gartenstadt Kremserberg
            ScheduleSignature(8845121L, "page:4", tripType = REGULAR),
            // LUP 2: Harland Amtshaus => St. Pölten Hauptbahnhof Süd
            ScheduleSignature(8845120L, "page:4", tripType = SHORT),
            // LUP 2: St. Pölten Gartenstadt Kremserberg => Harland Amtshaus (Sonntag)
            ScheduleSignature(9063950L, "page:5", scheduleType = SUNDAY_HOLIDAY),
            // LUP 2: St. Pölten Gartenstadt Kremserberg => Harland Amtshaus
            ScheduleSignature(8845122L, "page:5", tripType = REGULAR),
            // LUP 3: St. Pölten Stifterstraße => Ratzersdorf Ginstergasse
            ScheduleSignature(8845205L, "page:6"),
            // LUP 3: Ratzersdorf Ginstergasse => St. Pölten Stifterstraße
            ScheduleSignature(8845206L, "page:7", tripType = REGULAR),
            // LUP 3: Ratzersdorf Ginstergasse => St. Pölten Hauptbahnhof Süd
            ScheduleSignature(9063970L, "page:7", tripType = SHORT),
            // LUP 4: Unterradlberg Gewerbepark => St. Pölten Friedhof
            ScheduleSignature(8860850L, "page:8", tripType = REGULAR),
            // LUP 4: St. Pölten Hauptbahnhof Süd => St. Pölten Friedhof
            ScheduleSignature(9063985L, "page:8", tripType = SHORT),
            // LUP 4: St. Pölten Friedhof => Unterradlberg Gewerbepark
            ScheduleSignature(8860851L, "page:9", scheduleType = SUNDAY_HOLIDAY, tripType = REGULAR),
            // LUP 4: St. Pölten Friedhof => St. Pölten Hauptbahnhof Süd
            ScheduleSignature(9063986L, "page:9", scheduleType = SUNDAY_HOLIDAY, tripType = SHORT),
            // LUP 4: St. Pölten Friedhof => Unterradlberg Gewerbepark
            ScheduleSignature(8860851L, "page:9", tripType = REGULAR),
            // LUP 4: St. Pölten Friedhof => Pottenbrunn Josef-Trauttmansdorff-Straße
            ScheduleSignature(8860849L, "page:9", tripType = SHORT),
            // LUP 5: Spratzern Missongasse => St. Pölten Traisenpark/Schärf-Straße
            ScheduleSignature(8866749L, "page:10"),
            // LUP 5: St. Pölten Traisenpark/Schärf-Straße => Spratzern Missongasse
            ScheduleSignature(8866751L, "page:11"),
            // LUP 6: St. Pölten Hauptbahnhof Süd => St. Pölten Traisenpark/Schärf-Straße
            ScheduleSignature(9064043L, "page:12", scheduleType = SUNDAY_HOLIDAY),
            // LUP 6: St. Pölten Friedhof => St. Pölten Traisenpark/Schärf-Straße
            ScheduleSignature(8867234L, "page:12"),
            // LUP 6: St. Pölten Traisenpark/Schärf-Straße => St. Pölten Hauptbahnhof Süd
            ScheduleSignature(8867063L, "page:13", scheduleType = SUNDAY_HOLIDAY),
            // LUP 6: St. Pölten Traisenpark/Schärf-Straße => St. Pölten Hauptbahnhof Süd
            ScheduleSignature(8867063, "page:13", tripType = SHORT),
            // LUP 6: St. Pölten Traisenpark/Schärf-Straße => St. Pölten Friedhof
            ScheduleSignature(8867066L, "page:13", tripType = REGULAR),
            // LUP 7: Ratzersdorf Gewerbepark => St. Pölten VAZ/Rödlgasse
            ScheduleSignature(8871350L, "page:15", scheduleType = COMMUTER_WEEKDAY),
            // LUP 7: Ratzersdorf Gewerbepark => St. Pölten VAZ/Rödlgasse
            ScheduleSignature(8871350L, "page:15", scheduleType = COMMUTER_SATURDAY),
            // LUP 7: Pottenbrunn Bahnhof => St. Pölten Hauptbahnhof Süd
            ScheduleSignature(9064056L, "page:15", scheduleType = SUNDAY_HOLIDAY),
            // LUP 7: Pottenbrunn Bahnhof => St. Pölten VAZ/Rödlgasse
            ScheduleSignature(8871351L, "page:15"),
            // LUP 7: St. Pölten Hauptbahnhof Süd => Pottenbrunn Bahnhof
            ScheduleSignature(9064057L, "page:16", scheduleType = SUNDAY_HOLIDAY),
            // LUP 7: St. Pölten VAZ/Rödlgasse => Ratzersdorf Gewerbepark
            ScheduleSignature(8871352L, "page:16", tripType = SHORT),
            // LUP 7: St. Pölten VAZ/Rödlgasse => Pottenbrunn Bahnhof
            ScheduleSignature(8871353L, "page:16", tripType = REGULAR),
            // LUP 8: Unterradlberg Gewerbepark => Harland Amtshaus
            ScheduleSignature(8871717L, "page:17"),
            // LUP 8: Harland Amtshaus => Unterradlberg Gewerbepark
            ScheduleSignature(8871719L, "page:18"),
            // LUP 8: Unterradlberg Gewerbepark => Harland Amtshaus (über St. Pölten Landsbergerstraße)
            ScheduleSignature(9064113L, "page:19", tripType = REGULAR),
            // LUP 8: Unterradlberg Gewerbepark => St. Pölten Hauptbahnhof Süd
            ScheduleSignature(9064115L, "page:19", tripType = SHORT),
            // LUP 8: Harland Amtshaus => Unterradlberg Gewerbepark (über St. Pölten Landsbergerstraße)
            ScheduleSignature(9064114L, "page:20", tripType = REGULAR),
            // LUP 8: St. Pölten Hauptbahnhof Süd => Unterradlberg Gewerbepark (8871718)
            ScheduleSignature(8871718L, "page:20", tripType = SHORT),
            // LUP 9: St. Pölten Rudolf-Tornar-Straße => St. Pölten Hauptbahnhof Süd
            ScheduleSignature(9064119L, "page:21:UPPER_HALF", scheduleType = SUNDAY_HOLIDAY, tripType = SHORT),
            // LUP 9: St. Pölten Rudolf-Tornar-Straße => Hafing Ort
            ScheduleSignature(8871898L, "page:21:UPPER_HALF"),
            // LUP 9: Hafing Ort => St. Pölten Hauptbahnhof Süd
            ScheduleSignature(8871899L, "page:21:LOWER_HALF", tripType = SHORT),
            // LUP 9: Hafing Ort => St. Pölten Rudolf-Tornar-Straße
            ScheduleSignature(8871900L, "page:21:LOWER_HALF"),
            // LUP 12: St. Pölten Einkaufszentrum Süd => Viehofen Johannesplatz
            ScheduleSignature(8872118L, "page:24:UPPER_HALF"),
            // LUP 12: Viehofen Johannesplatz => St. Pölten Einkaufszentrum Süd
            ScheduleSignature(8872119L, "page:24:LOWER_HALF"),
            // LUP 13: St. Pölten Hugo-Wolf-Straße => Ragelsdorf Ort
            ScheduleSignature(8872316L, "page:25:UPPER_HALF"),
            // LUP 13: Ragelsdorf Ort => St. Pölten Hugo-Wolf-Straße
            ScheduleSignature(8872318L, "page:25:LOWER_HALF")
        )
    }

    override fun getRoutes(): RouteData {
        val extractor = OsmRouteReader(osmPath)

        val tripIds = SCHEDULES.map { it.routeId }
        // get all bus routes which match our set of known ids
        return extractor.read(object: RoutePredicate {
            override fun matches(routeTag: String?, id: Long): Boolean {
                return routeTag == "bus" && tripIds.contains(id)
            }
        })
    }

    override fun getSchedules(): Schedules {
        // parse schedules from pdf
        val parser = PdfPlanParser()
        val plan = FileInputStream(schedulePath)

        val schedules = parser.parse(plan, PAGE_DESCRIPTORS)

        return schedules.let {
            it.copy(schedules = it.schedules.flatMap(::postProcess))
        }
    }

    override fun getStopMatcher(): StopMatcher {
        return LupStopMatcher()
    }

    override fun getRouteMatcher(routes: List<Route>): RouteMatcher {
        return RouteMatcher(SCHEDULES, routes)
    }

    override fun getAgency(): GtfsAgency {
        return GtfsAgency(
            "stp-lup",
            "LUP St. Pölten",
            "https://www.st-poelten.at/gv-buergerservice/verkehr-mobilitaet-und-reisen/lup",
            "Europe/Vienna",
            "de"
        )
    }

    override fun getFares(): List<Fare> {
        return listOf(
            Fare(
                "vollpreis-bus",
                1.8f,
                Currency.getInstance("EUR"),
                PaymentMethod.PAY_ON_BOARD
            )
        )
    }

    override fun getFeedInfo(): GtfsFeedInfo {
        return GtfsFeedInfo(
            publisherName = "Kristoffer Kleine",
            publisherUrl = "https://github.com/kkris/osm-gtfs",
            language = "de",
            startDate = startDate,
            endDate = endDate,
            version = "1"
        )
    }

    override fun getFeedValidity(): Pair<LocalDate, LocalDate> {
        return Pair(startDate, endDate)
    }

    private fun postProcess(schedule: RegularSchedule): List<RegularSchedule> {
        return when (schedule.source) {
            "page:3" -> {
                val modified = schedule.copy(
                    stops = schedule.stops.filter { !it.name.contains("Wörther Straße") } // there is an error in the schedule and this this is probably not serviced on this route
                )

                return listOf(modified)
            }
            "page:4" -> {
                if (schedule.operationType == SUNDAY_HOLIDAY) {
                    return listOf(schedule)
                }

                // split weekday and saturday schedule into short and long trips
                val (regular, short) = LastTripsScheduleSplitter(1)
                    .split(schedule)

                return listOf(
                    regular,
                    short.copy(stops = short.stops.dropLast(1)) // drop last stop because it is a duplicate of Hauptbahnhof Süd TODO: needed?
                )
            }
            "page:7" -> {
                if (schedule.operationType != SUNDAY_HOLIDAY) {
                    return listOf(schedule)
                }

                // split sunday schedule into short and long trips
                return LastTripsScheduleSplitter(1)
                    .split(schedule)
            }
            "page:8" -> {
                if (schedule.operationType != SUNDAY_HOLIDAY) {
                    return listOf(schedule)
                }

                // split schedule into short trip which starts at a later stop (Hauptbahnhof)
                return MergingTripScheduleSplitter { stop, type ->
                    when (type) {
                        REGULAR -> listOf(min(stop.intervals))
                        SHORT -> listOf(max(stop.intervals))
                    }
                }.split(schedule)
            }
            "page:9" -> {
                when (schedule.operationType) {
                    SUNDAY_HOLIDAY -> {
                        val (regular, short) = LastTripsScheduleSplitter(2).split(schedule)

                        // split regular schedule because it diverges and discard the short schedule as it is already covered by
                        // the short schedule from above
                        val (regularSplitSchedule, _) = MergingTripScheduleSplitter { stop, type ->
                            when (type) {
                                REGULAR -> listOf(max(stop.intervals))
                                SHORT -> listOf(min(stop.intervals))
                            }
                        }.split(regular)

                        return listOf(
                            regularSplitSchedule,
                            short
                        )

                    }
                    else -> {
                        return LastTripsScheduleSplitter(1)
                            .split(schedule)
                    }
                }
            }
            "page:13" -> {
                if (schedule.operationType != WEEKDAY) {
                    return listOf(schedule)
                }

                return OverlappingTripScheduleSplitter().split(schedule)
            }
            "page:15" -> {
                val modified = schedule.copy(
                    stops = schedule.stops.filter { !it.name.contains("Wirtschaftskammer") } // there is an error in the schedule and this this is probably not serviced on this route
                )

                return listOf(modified)
            }
            "page:16" -> {
                if (schedule.operationType != WEEKDAY) {
                    return listOf(schedule)
                }

                // split weekday schedule into short and long trips
                return LastTripsScheduleSplitter(2)
                    .split(schedule)
            }
            "page:17", "page:18", "page:20" -> {
                // TODO: The OSM trip is missing two stops, filter them out for now
                listOf(
                    schedule.copy(stops = schedule.stops.filter {
                        !listOf("Stadtpark", "Neugebäudeplatz").contains(it.name)
                    })
                )
            }
            "page:19" -> {
                val schedule = schedule.copy(stops = schedule.stops.filter {
                    !listOf("Stadtpark", "Neugebäudeplatz").contains(it.name)
                })

                return OverlappingTripScheduleSplitter().split(schedule)
            }
            "page:21:UPPER_HALF" -> {
                if (schedule.operationType != SUNDAY_HOLIDAY) {
                    return listOf(schedule)
                }

                return OverlappingTripScheduleSplitter().split(schedule)
            }
            "page:21:LOWER_HALF" -> {
                if (schedule.operationType == SUNDAY_HOLIDAY) {
                    return listOf(schedule)
                }

                return LastTripsScheduleSplitter(1).split(schedule)
            }
            else -> listOf(schedule)
        }
    }
}
