package app.ryanm.homeinventory

import android.graphics.Bitmap
import androidx.annotation.ColorInt
import com.google.zxing.BarcodeFormat
import com.google.zxing.oned.Code128Writer

class Barcode (val barcode: String){
    fun generate(width: Int, height: Int, @ColorInt barcodeColor: Int, @ColorInt backgroundColor: Int): Bitmap {
        val bitmatrix = Code128Writer().encode(barcode, BarcodeFormat.CODE_128, width, height)

        val pixels = IntArray(bitmatrix.width * bitmatrix.height)
        for(y in 0 until bitmatrix.height) {
            val offset = y * bitmatrix.width
            for(x in 0 until bitmatrix.width) {
                pixels[offset + x] = if(bitmatrix.get(x, y)) barcodeColor else backgroundColor
            }
        }

        val bitmap = Bitmap.createBitmap(
            bitmatrix.width,
            bitmatrix.height,
            Bitmap.Config.ARGB_8888
        )

        bitmap.setPixels(pixels, 0, bitmatrix.width, 0, 0, bitmatrix.width, bitmatrix.height)

        return bitmap
    }
}