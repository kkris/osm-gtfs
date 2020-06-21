package org.kkris.osmgtfs.lup.pdf.parser

import org.kkris.osmgtfs.lup.pdf.ParserException
import org.kkris.osmgtfs.lup.pdf.model.PageSection
import org.kkris.osmgtfs.lup.pdf.model.PageType
import technology.tabula.Page
import technology.tabula.extractors.BasicExtractionAlgorithm

class Tokenizer {
    companion object {
        private const val CELL_HEIGHT_FACTOR = 2.7f
        private const val DEPARTURE_TABLE_HEADER_MARKER = "von"
    }

    private val extractionAlgorithm: BasicExtractionAlgorithm = BasicExtractionAlgorithm()

    fun tokenize(page: Page, section: PageSection): List<List<String>> {
        val (page, offset) = getPageSection(page, section.type)
        val (y, cellHeight) = getStartAndHeight(page)

        var current = y
        val stride = CELL_HEIGHT_FACTOR * cellHeight
        val rows = mutableListOf<List<String>>()

        // loop over page in rows of height $stride and extract the text
        var row = getRow(page, current, stride)
        while (current <= page.height - stride - section.bottomOffset + offset) {
            row?.let {
                rows.add(it.cells)

                current = it.y + it.height
            } ?: run {
                current += stride
            }

            row = getRow(page, current, stride)
        }

        return rows
    }

    private fun getRow(page: Page, yOffset: Float, height: Float): RawRow? {
        val tables = extractionAlgorithm.extract(page.getArea(yOffset, 0f, yOffset + height, page.getWidth().toFloat()))
        if (tables.size != 1) {
            throw RuntimeException("todo")
        }

        val table = tables[0]
        if (table.rowCount > 1) {
            throw RuntimeException("Matched too many rows")
        }

        if (table.rowCount == 0 || table.colCount == 0) {
            // no content in this row
            return null
        }

        val row = table.rows[0]
        return RawRow(
            row.map { it.text },
            row[0].y,
            row[0].height
        )
    }

    private fun getStartAndHeight(page: Page): Pair<Float, Float> {
        val tables = extractionAlgorithm.extract(page)
        if (tables.size != 1) {
            throw ParserException("Extraction algorithm found more than one table.")
        }

        val cell = tables[0].rows.flatten().find { cell ->
            cell.text.contains(DEPARTURE_TABLE_HEADER_MARKER)
        } ?: throw ParserException("Could not find cell marking departure header")

        return Pair((cell.y + cell.getHeight()).toFloat(), cell.getHeight().toFloat())
    }

    private fun getPageSection(page: Page, type: PageType): Pair<Page, Float> {
        return when (type) {
            PageType.FULL -> Pair(page, 0f)
            PageType.UPPER_HALF -> Pair(page.getArea(0f, 0f, page.getHeight().toFloat() / 2, page.getWidth().toFloat()), 0f)
            PageType.LOWER_HALF -> Pair(page.getArea(page.getHeight().toFloat() / 2, 0f, page.getHeight().toFloat(), page.getWidth().toFloat()), page.getHeight().toFloat() / 2)
        }
    }
}

private data class RawRow(
    val cells: List<String>,
    val y: Float,
    val height: Float
)