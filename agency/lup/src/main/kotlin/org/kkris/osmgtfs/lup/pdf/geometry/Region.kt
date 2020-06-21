package org.kkris.osmgtfs.lup.pdf.geometry

import technology.tabula.Rectangle

data class Region(
    val topLeft: Point,
    val bottomRight: Point
) {
    private fun width(): Double {
        return bottomRight.x - topLeft.x
    }

    private fun height(): Double {
        return bottomRight.y - topLeft.y
    }

    fun toRectangle(): Rectangle {
        return Rectangle(
            topLeft.y.toFloat(),
            topLeft.x.toFloat(),
            width().toFloat(),
            height().toFloat()
        )
    }
}