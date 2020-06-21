package org.kkris.osmgtfs.gtfs.model

import org.kkris.osmgtfs.common.model.ScheduleOperationType
import org.onebusaway.gtfs.model.AgencyAndId
import org.onebusaway.gtfs.model.ServiceCalendar
import org.onebusaway.gtfs.model.calendar.ServiceDate
import java.time.LocalDate

data class GtfsCalendar(
    val type: ScheduleOperationType,
    val feedValidity: Pair<LocalDate, LocalDate>
): GtfsEntity<ServiceCalendar>() {
    override fun toEntity(agency: GtfsAgency): ServiceCalendar {
        val entity = ServiceCalendar()

        val (start, end) = feedValidity

        entity.serviceId = AgencyAndId(agency.id, id())
        entity.startDate = ServiceDate(start.year, start.monthValue, start.dayOfMonth)
        entity.endDate = ServiceDate(end.year, end.monthValue, end.dayOfMonth)
        entity.monday = if (type.isWeekday()) 1 else 0
        entity.tuesday = if (type.isWeekday()) 1 else 0
        entity.wednesday = if (type.isWeekday()) 1 else 0
        entity.thursday = if (type.isWeekday()) 1 else 0
        entity.friday = if (type.isWeekday()) 1 else 0
        entity.saturday = if (type.isSaturday()) 1 else 0
        entity.sunday = if (type == ScheduleOperationType.SUNDAY_HOLIDAY) 1 else 0

        return entity
    }

    override fun id(): String {
        // TODO: handle multiple validity periods
        return when (type) {
            ScheduleOperationType.WEEKDAY -> "wkd"
            ScheduleOperationType.SATURDAY -> "sat"
            ScheduleOperationType.SUNDAY_HOLIDAY -> "sun"
            ScheduleOperationType.COMMUTER_WEEKDAY -> "commuter-wkd"
            ScheduleOperationType.COMMUTER_SATURDAY -> "commuter-sat"
        }
    }
}