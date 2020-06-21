package org.kkris.osmgtfs.gtfs.model

import org.onebusaway.gtfs.model.FeedInfo
import org.onebusaway.gtfs.model.calendar.ServiceDate
import java.time.LocalDate

data class GtfsFeedInfo(
    private val publisherName: String,
    private val publisherUrl: String,
    private val language: String,
    private val startDate: LocalDate,
    private val endDate: LocalDate,
    private val version: String
) {

    fun toEntity(): FeedInfo {
        val entity = FeedInfo()

        entity.publisherName = publisherName
        entity.publisherUrl = publisherUrl
        entity.lang = language
        entity.startDate = ServiceDate(startDate.year, startDate.monthValue, startDate.dayOfMonth)
        entity.endDate = ServiceDate(endDate.year, endDate.monthValue, endDate.dayOfMonth)
        entity.version = version

        return entity
    }
}