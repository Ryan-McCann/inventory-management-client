package app.ryanm.homeinventory.barcode

import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.pdf.PdfDocument
import org.xmlpull.v1.XmlPullParser

data class Cell(val barX: Int, val barY: Int, val textX: Int, val textY: Int)

/**
 * Class for creating labels from barcodes. All width and height variables are in inches,
 * which will be converted to pixels at 300ppi when generating the barcodes.
 */
class LabelTemplate (val xmlParser: XmlPullParser) {
    fun generateLabels(barcodes: ArrayList<Barcode>, barcodeColor: Int, backgroundColor: Int): PdfDocument {
        var tag: String? = ""
        var event = xmlParser.eventType

        var barcodeHeight: Float = 0f
        var barcodeWidth = 0f
        var cellsPerPage = 0
        var textSize = ""
        var width = 0f
        var height = 0f

        var cells = ArrayList<Cell>()

        while(event != XmlPullParser.END_DOCUMENT) {
            tag = xmlParser.name
            when(event) {
                XmlPullParser.START_TAG -> if (tag == "page") {
                    barcodeWidth = xmlParser.getAttributeValue(null, "barcodeWidth").toFloat()
                    barcodeHeight = xmlParser.getAttributeValue(null, "barcodeHeight").toFloat()
                    cellsPerPage = xmlParser.getAttributeValue(null, "cells").toInt()
                    textSize = xmlParser.getAttributeValue(null, "textSize")
                    width = xmlParser.getAttributeValue(null, "width").toFloat()
                    height = xmlParser.getAttributeValue(null, "height").toFloat()
                } else if(tag == "cell") {
                    val barX = xmlParser.getAttributeValue(null, "barX").toInt()
                    val barY = xmlParser.getAttributeValue(null, "barY").toInt()
                    val textX = xmlParser.getAttributeValue(null, "textX").toInt()
                    val textY = xmlParser.getAttributeValue(null, "textY").toInt()
                    cells.add(Cell(barX, barY, textX, textY))
                }
            }
            event = xmlParser.next()
        }

        var pageNumber = 0

        val document = PdfDocument()
        var pageInfo = PdfDocument.PageInfo.Builder((width).toInt(), (height).toInt(), pageNumber).create()
        var page = document.startPage(pageInfo)

        var cellNumber = 0

        for(i in 0 until barcodes.size) {
            val bitmap = barcodes[i].generate(barcodeWidth.toInt(), barcodeHeight.toInt(), barcodeColor, backgroundColor)
            bitmap.density = 72
            val left = (cells[cellNumber].barX - barcodeWidth/2)
            val top = (cells[cellNumber].barY - barcodeHeight/2)
            val right = left + barcodeWidth
            val bottom = top + barcodeHeight

            val destRect = RectF(left, top, right, bottom)

            page.canvas.drawBitmap(bitmap, null, destRect, null)

            val textPaint = Paint()
            textPaint.textAlign = Paint.Align.CENTER

            page.canvas.drawText(barcodes[i].label, cells[cellNumber].textX.toFloat(), cells[cellNumber].textY.toFloat(), textPaint)

            if(cellNumber < cellsPerPage-1) {
                cellNumber++
            } else if(cellNumber >= (cellsPerPage-1) && i < barcodes.size) {
                cellNumber = 0
                pageNumber++
                document.finishPage(page)
                pageInfo = PdfDocument.PageInfo.Builder((width).toInt(), (height).toInt(), pageNumber).create()
                page = document.startPage(pageInfo)
            }
        }

        document.finishPage(page)
        return document
    }
}