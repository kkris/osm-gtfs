package org.kkris.osmgtfs.common

import io.kotest.core.spec.style.StringSpec
import io.kotest.inspectors.forAll
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.comparables.shouldBeGreaterThanOrEqualTo
import io.kotest.matchers.comparables.shouldBeLessThanOrEqualTo
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.property.Arb
import io.kotest.property.arbitrary.arb
import io.kotest.property.checkAll
import java.time.Duration
import java.time.LocalTime
import kotlin.random.nextInt

class UtilTest: StringSpec({
    "get soonest" {
        checkAll(randomData()) { data ->
            val soonest = Util.getSoonest(data.first, data.second, data.intervals)
            soonest.shouldNotBeNull()
            soonest.shouldBeGreaterThanOrEqualTo(data.first)
            soonest.shouldBeLessThanOrEqualTo(data.second)
            soonest.let {
                data.intervals.shouldContain(it.minute)
            }
        }
    }
    "get timeseries" {
        checkAll(randomData()) { data ->
            val series = Util.generateTimeseries(data.first, data.second, data.intervals)

            series.forAll { time ->
                time.shouldBeGreaterThanOrEqualTo(data.first)
                time.shouldBeLessThanOrEqualTo(data.second)
                time.let {
                    data.intervals.shouldContain(it.minute)
                }
            }
        }
    }
})

private fun randomData(): Arb<TestData> {
    return arb { rs ->
        generateSequence {
            val after = LocalTime.MIDNIGHT.plusMinutes(rs.random.nextInt(0..560).toLong())
            val latest = after.plusMinutes(rs.random.nextInt(0, 120).toLong())

            val candidates = (0..Duration.between(after, latest).toMinutes()).map {
                after.plusMinutes(it).minute
            }

            val intervalLength = rs.random.nextInt(0..5)
            val intervals = (0..intervalLength).map {
                candidates[rs.random.nextInt(candidates.indices)]
            }.distinct().sorted()

            TestData(
                after,
                latest,
                intervals
            )
        }
    }
}

private data class TestData(
    val first: LocalTime,
    val second: LocalTime,
    val intervals: List<Int>
)