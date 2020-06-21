package org.kkris.osmgtfs.gtfs.util

import java.security.MessageDigest

class DeterministicIdGenerator {
    companion object {
        private const val ID_LENGTH = 8
    }

    fun getId(identifier: Any): String {
        return hash(identifier.toString()).take(ID_LENGTH)
    }

    private fun hash(value: String): String {
        return MessageDigest
            .getInstance("SHA-256")
            .digest(value.toByteArray())
            .fold("", { str, it -> str + "%02x".format(it) })
    }
}