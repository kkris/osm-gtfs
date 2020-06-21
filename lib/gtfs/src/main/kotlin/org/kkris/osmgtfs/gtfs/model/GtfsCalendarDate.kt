package org.kkris.osmgtfs.gtfs.model

import org.kkris.osmgtfs.common.identifier
import org.onebusaway.gtfs.model.AgencyAndId
import org.onebusaway.gtfs.model.ServiceCalendarDate
import org.onebusaway.gtfs.model.calendar.ServiceDate
import java.time.LocalDate

data class GtfsCalendarDate(
    val calendar: GtfsCalendar,
    val date: LocalDate,
    val type: ExceptionType
): GtfsEntity<ServiceCalendarDate>() {
    override fun toEntity(agency: GtfsAgency): ServiceCalendarDate {
        val entity = ServiceCalendarDate()

        entity.serviceId = AgencyAndId(agency.id, calendar.id())
        entity.date = ServiceDate(date.year, date.monthValue, date.dayOfMonth)
        entity.exceptionType = when (type) {
            ExceptionType.ADDED -> 1
            ExceptionType.REMOVED -> 2
        }

        return entity
    }

    override fun id(): String {
        return type.name.identifier()
    }

    enum class ExceptionType {
        ADDED,
        REMOVED
    }
}