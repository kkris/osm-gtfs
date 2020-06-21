package org.kkris.osmgtfs.agency

import de.jollyday.HolidayManager
import de.jollyday.ManagerParameters
import java.time.LocalDate
import java.util.*

object Util {

    internal fun getHolidays(locale: Locale, start: LocalDate, end: LocalDate): List<LocalDate> {
        val holidays = (start.year..end.year).flatMap { year ->
            getHolidaysInYear(locale, year)
        }

        return holidays
            .filter { it in start..end }
            .sorted()
    }

    private fun getHolidaysInYear(locale: Locale, year: Int): List<LocalDate> {
        val parameter = ManagerParameters.create(locale)

        return HolidayManager.getInstance(parameter).getHolidays(year)
            .map { it.date }
    }
}