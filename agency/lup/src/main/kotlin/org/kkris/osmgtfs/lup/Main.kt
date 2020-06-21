package org.kkris.osmgtfs.lup

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import com.github.ajalt.clikt.parameters.types.file
import mu.KotlinLogging
import java.time.LocalDate

class Main: CliktCommand() {
    private val scheduleFile by option("--schedule-path", help = "Path to LUP schedule in PDF format").file().required()
    private val osmPbfFile by option("--osm-path", help = "Path to OpenStreetMaps file in pbf format").file().required()
    private val outputDirectory by option("--output-dir", help = "Path to the output directory to write the generated feed").file().required()

    private val startDate by option("--start-date", help = "Date where feed validity starts").required()
    private val endDate by option("--end-date", help = "Date where feed validity ends (inclusive)").required()

    private val logger = KotlinLogging.logger {}

    override fun run() {
        logger.info { "Generating GTFS feed from OSM data and PDF schedule" }

        val generator = LupGtfsGenerator(
            scheduleFile.absolutePath,
            osmPbfFile.absolutePath,
            LocalDate.parse(startDate),
            LocalDate.parse(endDate)
        )

        generator.generate(outputDirectory.absolutePath)
    }
}

fun main(args: Array<String>) {
    Main().main(args)
}
