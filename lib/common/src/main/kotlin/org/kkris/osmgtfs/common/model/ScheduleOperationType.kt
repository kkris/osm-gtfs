package org.kkris.osmgtfs.common.model

// TODO: add support for school bus operation mode
/**
 * Indicates the type of operation of a schedule
 */
enum class ScheduleOperationType {
    WEEKDAY,
    SATURDAY,
    SUNDAY_HOLIDAY,
    COMMUTER_WEEKDAY,
    COMMUTER_SATURDAY;

    fun isWeekday(): Boolean {
        return this in listOf(WEEKDAY, COMMUTER_WEEKDAY)
    }

    fun isSaturday(): Boolean {
        return this in listOf(SATURDAY, COMMUTER_SATURDAY)
    }
}