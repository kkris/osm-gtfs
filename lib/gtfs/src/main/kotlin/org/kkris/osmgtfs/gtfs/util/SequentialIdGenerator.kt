package org.kkris.osmgtfs.gtfs.util

import java.util.*
import kotlin.math.ceil
import kotlin.math.log10

class SequentialIdGenerator(
    private val maxIds: Int = 1_000_000 // maximum number of ids which must be generated. enables generation of smaller ids
) {
    private var sequence = 0
    private val cache = mutableMapOf<Int, String>()

    fun getId(vararg values: Any?): String {
        val identity = Objects.hash(*values)

        return cache[identity] ?: run {
            // id not yet computed, add it to cache and return it
            val id = getNextId()
            cache[identity] = id

            id
        }
    }

    private fun getNextId(): String {
        val seq = sequence++

        val idLength = ceil(log10(maxIds.toDouble())).toInt()
        return seq.toString().padStart(idLength, '0')
    }
}