package org.kkris.osmgtfs.lup.pdf.model

import org.kkris.osmgtfs.common.model.ScheduleOperationType


data class PageSection(
    val type: PageType = PageType.FULL,
    val columns: List<ScheduleOperationType> = listOf(ScheduleOperationType.WEEKDAY, ScheduleOperationType.SATURDAY, ScheduleOperationType.SUNDAY_HOLIDAY),
    // amount to cut-off at bottom of page (contains no useful information or confuses the extraction algorithm)
    val bottomOffset: Int = 5
)