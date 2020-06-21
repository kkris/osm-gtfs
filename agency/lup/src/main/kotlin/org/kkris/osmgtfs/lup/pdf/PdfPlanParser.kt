package org.kkris.osmgtfs.lup.pdf

import mu.KotlinLogging
import org.apache.pdfbox.pdmodel.PDDocument
import org.kkris.osmgtfs.common.model.schedule.Schedules
import org.kkris.osmgtfs.lup.pdf.model.PageDescriptor
import org.kkris.osmgtfs.lup.pdf.model.PageType
import org.kkris.osmgtfs.lup.pdf.parser.Parser
import technology.tabula.ObjectExtractor
import java.io.InputStream

class PdfPlanParser {
    private val logger = KotlinLogging.logger {}

    private val parser = Parser()

    @Throws(ParserException::class)
    fun parse(stream: InputStream, descriptors: List<PageDescriptor>): Schedules {
        PDDocument.load(stream).use { document ->
            val pages = ObjectExtractor(document)
                .extract()
                .asSequence()
                .toList()

            logger.info { "Loaded ${pages.size} pages with departures" }

            val schedules = descriptors.flatMap { descriptor ->
                descriptor.sections.flatMap { section ->
                    val page = pages.firstOrNull { it.pageNumber == descriptor.page } ?: throw ParserException("Failed to find page ${descriptor.page}")

                    logger.info { "Parsing schedules from $descriptor and $section" }

                    parser.parseSchedules(page, section).map {
                        // attach source location to schedule
                        if (section.type != PageType.FULL) {
                            it.copy(source = "page:${descriptor.page}:${section.type}")
                        } else it.copy(source = "page:${descriptor.page}")
                    }
                }
            }

            return Schedules(schedules)
        }
    }
}