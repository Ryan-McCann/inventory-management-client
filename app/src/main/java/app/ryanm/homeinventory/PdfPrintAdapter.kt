package app.ryanm.homeinventory

import android.content.Context
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.os.Bundle
import android.os.CancellationSignal
import android.os.ParcelFileDescriptor
import android.print.PageRange
import android.print.PrintAttributes
import android.print.PrintDocumentAdapter
import android.print.PrintDocumentInfo
import android.print.pdf.PrintedPdfDocument
import java.io.FileOutputStream
import java.io.IOException

class PdfPrintAdapter(val context: Context, private val document: PdfDocument): PrintDocumentAdapter() {
    private lateinit var printDoc: PrintedPdfDocument

    override fun onLayout(
        oldAttributes: PrintAttributes,
        newAttributes: PrintAttributes,
        cancellationSignal: CancellationSignal,
        callback: LayoutResultCallback?,
        bundle: Bundle?) {

        if(cancellationSignal.isCanceled){
            callback?.onLayoutCancelled()
            return
        }

        printDoc = PrintedPdfDocument(context, newAttributes)

        for(page in document.pages) {
            val pageInfo = PdfDocument.PageInfo.Builder(page.pageWidth, page.pageHeight, page.pageNumber).create()
            val printPage = printDoc.startPage(pageInfo)

            val paint = Paint()
            paint.textAlign = Paint.Align.CENTER
            printPage.canvas.drawText("Hello", 72f, 72f, paint)

            printDoc.finishPage(printPage)
        }

        //document.close()

        val info = PrintDocumentInfo.Builder(" file name")
            .setContentType(PrintDocumentInfo.CONTENT_TYPE_DOCUMENT)
            .setPageCount(document.pages.size)
            .build()

        callback?.onLayoutFinished(info, oldAttributes != newAttributes)

    }

    override fun onWrite(
        pages: Array<out PageRange>,
        destination: ParcelFileDescriptor,
        cancellationSignal: CancellationSignal,
        callback: WriteResultCallback) {

        try {
            document.writeTo(FileOutputStream(destination.fileDescriptor))
        } catch (e: IOException) {
            callback.onWriteFailed(e.toString())
            return
        } finally {
            document.close()
        }

        callback.onWriteFinished(pages)
    }
}