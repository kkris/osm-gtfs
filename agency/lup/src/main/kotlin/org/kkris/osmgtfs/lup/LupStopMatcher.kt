package org.kkris.osmgtfs.lup

import mu.KotlinLogging
import org.apache.commons.text.similarity.JaccardSimilarity
import org.kkris.osmgtfs.agency.matcher.StopMatcher
import org.kkris.osmgtfs.common.model.stop.Stop

class LupStopMatcher: StopMatcher {

    private val similarity = JaccardSimilarity()
    private val logger = KotlinLogging.logger {}

    override fun match(stopName: String, stops: List<Stop>): Stop? {
        return matchInternal(stopName, stops).also {
            logger.debug { "Matched '$stopName' to '${it?.name}'" }
        }
    }

    private fun matchInternal(stopName: String, stops: List<Stop>): Stop? {
        mappingOverride(stopName, stops)?.let {
            return it
        }

        val exactCandidates = getExactCandidates(stopName, stops)
        if (exactCandidates.size == 1) {
            return exactCandidates[0]
        } else if (exactCandidates.isNotEmpty()) {
            logger.warn { "Ambiguous stop name '$stopName'" }
            return exactCandidates[0]
        }

        getFuzzyCandidates(stopName, stops).firstOrNull()?.first?.let {
            return it
        }

        return null
    }

    private fun getExactCandidates(name: String, stops: List<Stop>): List<Stop> {
        return stops.filter { it.name.toLowerCase().contains(name.toLowerCase()) }
    }

    private fun getFuzzyCandidates(name: String, stops: List<Stop>): List<Pair<Stop, Double>> {
        return stops
            .map { Pair(it, similarity.apply(it.name.toLowerCase(), name.toLowerCase())) }
            .filter { it.second >= 0.5 }
            .sortedByDescending { it.second }
    }

    private fun mappingOverride(name: String, stops: List<Stop>): Stop? {
        return when (name) {
            "EKZ Süd" -> stops.find { it.name == "St. Pölten Einkaufszentrum Süd" }
            "Hauptbahnhof A" -> stops.find { it.name == "St. Pölten Hauptbahnhof Süd" && it.platformName == "A" }
            "Hauptbahnhof B" -> stops.find { it.name == "St. Pölten Hauptbahnhof Süd" && it.platformName == "B" }
            "Hauptbahnhof Süd A" -> stops.find { it.name == "St. Pölten Hauptbahnhof Süd" && it.platformName == "A" }
            "Hauptbahnhof Süd B" -> stops.find { it.name == "St. Pölten Hauptbahnhof Süd" && it.platformName == "B" }
            "Hbhf Nord/Kremser Landstr." -> stops.find { it.name == "St. Pölten Hauptbahnhof Kremser Landstraße" }
            "Kremser Landstraße" -> stops.find { it.name == "St. Pölten Kremser Landstraße" }
            else -> null
        }
    }
}