package org.kkris.osmgtfs.lup.pdf.parser.token

import org.kkris.osmgtfs.common.model.StopType
import org.kkris.osmgtfs.lup.pdf.ParserException
import java.time.LocalTime

sealed class Token {
    @Throws(ParserException::class)
    abstract fun parse(): Any
}

class IntervalToken(private val text: String): Token() {
    companion object {
        private val PATTERN = """.(\d\d)""".toRegex()

        fun matches(text: String)= PATTERN.matches(text)
    }

    override fun parse(): Int {
        return PATTERN.find(text)?.groupValues?.get(1)?.toInt() ?: throw ParserException()
    }
}

class TimeStarToken(private val text: String): Token() {
    companion object {
        private val PATTERN = """(\d\d:\d\d)\s*\*\s*""".toRegex()

        fun matches(text: String) = PATTERN.matches(text)
    }

    override fun parse(): LocalTime {
        throw UnsupportedOperationException("Use StarToken and TimeToken to separate this token instead")
    }
}

class TimeToken(private val text: String): Token() {
    companion object {
        private val PATTERN = """(\d\d:\d\d).*""".toRegex()

        fun matches(text: String) = PATTERN.matches(text)
    }

    override fun parse(): LocalTime {
        return LocalTime.parse(PATTERN.find(text)?.groupValues?.get(1))
    }
}

class StarToken(private val text: String): Token() {
    companion object {
        private val PATTERN = """.*\*\s*""".toRegex()

        fun matches(text: String) = PATTERN.matches(text)
    }

    override fun parse(): Boolean {
        return true
    }
}

class EntryTypeToken(private val text: String): Token() {
    companion object {
        private val PATTERN = """(an$|ab$)""".toRegex()

        fun matches(text: String) = PATTERN.matches(text)
    }

    override fun parse(): StopType {
        val match= PATTERN.find(text)?.groupValues?.get(1) ?: throw ParserException()

        return when (match) {
            "an" -> StopType.ARRIVAL
            "ab" -> StopType.DEPARTURE
            else -> throw ParserException("unreachable")
        }
    }
}

class TextToken(private val text: String): Token() {
    companion object {
        private val PATTERN = """(.+)""".toRegex()

        fun matches(text: String) = PATTERN.matches(text)
    }

    override fun parse(): String {
        return PATTERN.find(text)?.groupValues?.get(1) ?: throw ParserException()
    }
}