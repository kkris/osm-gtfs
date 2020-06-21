package org.kkris.osmgtfs.lup.pdf.parser

import org.kkris.osmgtfs.common.model.ScheduleOperationType
import org.kkris.osmgtfs.common.model.ScheduleOperationType.COMMUTER_SATURDAY
import org.kkris.osmgtfs.common.model.ScheduleOperationType.COMMUTER_WEEKDAY
import org.kkris.osmgtfs.common.model.StopType
import org.kkris.osmgtfs.common.model.schedule.RegularSchedule
import org.kkris.osmgtfs.common.model.schedule.ScheduleStop
import org.kkris.osmgtfs.lup.pdf.ParserException
import org.kkris.osmgtfs.lup.pdf.model.PageSection
import org.kkris.osmgtfs.lup.pdf.parser.token.*
import technology.tabula.Page
import java.time.LocalTime

class Parser {

    private val tokenizer = Tokenizer()

    fun parseSchedules(page: Page, section: PageSection): List<RegularSchedule> {
        val tokens = tokenizer.tokenize(page, section).map { row ->
            row.flatMap(this::parseToken).toMutableList()
        }

        val entries = mutableMapOf<ScheduleOperationType, MutableList<ScheduleStop>>().apply {
            section.columns.forEach { type ->
                this[type] = mutableListOf()
            }
        }

        tokens.forEach { row ->
            val stopName = consumeStop(row)
            val entryType = consumeEntryType(row)

            section.columns.forEach { scheduleType ->
                if (row.isNotEmpty()) {
                    val nextTwoTokensAreTimes = row.take(2).filterIsInstance(TimeToken::class.java).size == 2

                    if (nextTwoTokensAreTimes && scheduleType in listOf(COMMUTER_WEEKDAY, COMMUTER_SATURDAY)) {
                        // two times in a row, this row contains a commuter stop
                        val begin = consumeTime(row)
                        val intervals = listOf(begin.minute)
                        entries[scheduleType]?.add(
                            ScheduleStop(
                                stopName,
                                entryType,
                                begin,
                                begin,
                                intervals,
                                false
                            )
                        )
                    } else if (scheduleType !in listOf(COMMUTER_WEEKDAY, COMMUTER_SATURDAY)) {
                        val begin = consumeTime(row)
                        val intervals = consumeIntervals(row)
                        val end = consumeTime(row)
                        val hasConstraint = consumeStar(row)

                        entries[scheduleType]?.add(
                            ScheduleStop(
                                stopName,
                                entryType,
                                begin,
                                end,
                                intervals,
                                hasConstraint
                            )
                        )
                    }
                }
            }
        }

        return entries.map { (scheduleType, entries) ->
            RegularSchedule(
                scheduleType,
                entries
            )
        }
    }

    private fun consumeStop(tokens: MutableList<Token>): String {
        return when (val head = tokens.firstOrNull()) {
            is TextToken -> {
                consume(tokens, 1)
                head.parse()
            }
            else -> throw ParserException()
        }
    }

    private fun consumeEntryType(tokens: MutableList<Token>): StopType {
        return when (val head = tokens.firstOrNull()) {
            is EntryTypeToken -> {
                consume(tokens, 1)
                head.parse()
            }
            else -> StopType.DEPARTURE
        }
    }

    private fun consumeStar(tokens: MutableList<Token>): Boolean? {
        return when (val head = tokens.firstOrNull()) {
            is StarToken -> {
                consume(tokens, 1)
                head.parse()
            }
            else -> null
        }
    }

    private fun consumeTime(tokens: MutableList<Token>): LocalTime {
        return when (val head = tokens.firstOrNull()) {
            is TimeToken -> {
                consume(tokens, 1)
                head.parse()
            }
            else -> throw ParserException()
        }
    }

    private fun consumeIntervals(tokens: MutableList<Token>): List<Int> {
        val intervals = tokens
            .takeWhile { it is IntervalToken }
            .filterIsInstance<IntervalToken>()
            .map { it.parse() }
        consume(tokens, intervals.size)

        return intervals
    }

    private fun consume(tokens: MutableList<Token>, count: Int) {
        repeat(count) { tokens.removeAt(0) }
    }

    private fun parseToken(text: String): List<Token> {
        return when {
            IntervalToken.matches(text) -> listOf(IntervalToken(text))
            TimeStarToken.matches(text) -> listOf(TimeToken(text), StarToken(text))
            TimeToken.matches(text) -> listOf(TimeToken(text))
            EntryTypeToken.matches(text) -> listOf(EntryTypeToken(text))
            TextToken.matches(text) -> listOf(TextToken(text))
            else -> throw ParserException()
        }
    }
}