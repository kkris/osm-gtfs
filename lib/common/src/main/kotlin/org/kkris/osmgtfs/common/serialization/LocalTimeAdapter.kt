package org.kkris.osmgtfs.common.serialization

import com.squareup.moshi.FromJson
import com.squareup.moshi.ToJson
import java.time.LocalTime
import java.time.format.DateTimeFormatter

class LocalTimeAdapter {

    @ToJson
    fun toJson(value: LocalTime): String {
        return DateTimeFormatter.ISO_LOCAL_TIME.format(value)
    }

    @FromJson
    fun fromJson(value: String): LocalTime {
        return LocalTime.parse(value, DateTimeFormatter.ISO_LOCAL_TIME)
    }
}