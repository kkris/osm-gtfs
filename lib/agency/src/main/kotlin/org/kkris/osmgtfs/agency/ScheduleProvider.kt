package org.kkris.osmgtfs.agency

import org.kkris.osmgtfs.common.model.schedule.Schedules

interface ScheduleProvider {
    fun getSchedules(): Schedules
}