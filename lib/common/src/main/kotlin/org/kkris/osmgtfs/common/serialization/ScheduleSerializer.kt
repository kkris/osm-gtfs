package org.kkris.osmgtfs.common.serialization

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import org.kkris.osmgtfs.common.model.schedule.Schedules
import java.io.File

object ScheduleSerializer {

    private val adapter by lazy {
        Moshi.Builder()
            .add(LocalTimeAdapter())
            .add(KotlinJsonAdapterFactory())
            .build()
            .adapter(Schedules::class.java)
    }

    fun load(path: String): Schedules? {
        val file = File(path)

        try {
            return adapter.fromJson(file.readText())
        } catch(e: Exception) {
            throw RuntimeException("Failed to read schedules", e)
        }
    }

    fun save(path: String, schedules: Schedules) {
        val file = File(path)
        file.parentFile.mkdirs()

        file.writeText(adapter.toJson(schedules))
    }
}