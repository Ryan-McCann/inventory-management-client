package app.ryanm.homeinventory.barcode

import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage

typealias BarcodeListener = (barcode: String) -> Unit

class BarcodeScanner (private val barcodeListener: BarcodeListener) :ImageAnalysis.Analyzer {
    private val scanner = BarcodeScanning.getClient()

    override fun  analyze(imageProxy: ImageProxy) {
        @androidx.camera.core.ExperimentalGetImage
        val mediaImage = imageProxy.image
        if(mediaImage != null) {
            val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)

            scanner.process(image)
                .addOnSuccessListener { barcodes ->
                    for (barcode in barcodes){
                        barcodeListener(barcode.displayValue ?: "")
                    }
                }
                .addOnFailureListener {

                }
                .addOnCompleteListener {
                    imageProxy.close()
                }
        }
    }
}