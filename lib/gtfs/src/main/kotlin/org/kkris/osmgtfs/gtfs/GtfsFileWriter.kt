package org.kkris.osmgtfs.gtfs

import org.kkris.osmgtfs.gtfs.model.*
import org.onebusaway.gtfs.serialization.GtfsWriter
import java.io.File

class GtfsFileWriter(private val path: String) {

    fun write(
        agency: GtfsAgency,
        stops: List<GtfsStop>,
        routes: List<GtfsRoute>,
        trips: List<GtfsTrip>,
        stopTimes: List<GtfsStopTime>,
        calendars: List<GtfsCalendar>,
        feedInfo: GtfsFeedInfo,
        calendarDates: List<GtfsCalendarDate> = emptyList(),
        shapes: List<GtfsShapePoint> = emptyList(),
        fares: List<GtfsFare> = emptyList(),
        fareRules: List<GtfsFareRule> = emptyList()
    ) {
        val writer = GtfsWriter()
        writer.setOutputLocation(File(path))

        writer.handleEntity(agency.toEntity())
        stops.forEach { writer.handleEntity(it.toEntity(agency)) }
        routes.forEach { writer.handleEntity(it.toEntity(agency)) }
        trips.forEach { writer.handleEntity(it.toEntity(agency)) }
        stopTimes.forEach { writer.handleEntity(it.toEntity(agency)) }
        calendars.forEach { writer.handleEntity(it.toEntity(agency)) }
        calendarDates.forEach { writer.handleEntity(it.toEntity(agency)) }
        shapes.forEach { writer.handleEntity(it.toEntity(agency)) }
        fares.forEach { writer.handleEntity(it.toEntity(agency)) }
        fareRules.forEach { writer.handleEntity(it.toEntity(agency)) }
        writer.handleEntity(feedInfo.toEntity())

        writer.close()
    }
}