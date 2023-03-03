package app.ryanm.homeinventory.ui

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import app.ryanm.homeinventory.R
import app.ryanm.homeinventory.inventory.Inventory
import app.ryanm.homeinventory.inventory.Item
import app.ryanm.homeinventory.inventory.Shelf
import app.ryanm.homeinventory.inventory.Shelving
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicBoolean

typealias BarcodeListener = (barcode: String) -> Unit

class ScanFragment : Fragment() {
    private var processingBarcode = AtomicBoolean(false)
    private lateinit var cameraExecutor: ExecutorService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        cameraExecutor = Executors.newSingleThreadExecutor()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_scan, container, false)

        /**
         * Setting onClick listener for the barcode scanner button
         * Will set visibility of the button to "gone" and make scanner
         * preview visible.
         * Also sets up the barcode scanner using the preview view
         */
        val scanButton: ImageButton = view.findViewById(R.id.scanButton)
        val scanPreview: PreviewView = view.findViewById(R.id.scanPreview)

        scanButton.setOnClickListener {

            if(ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_DENIED){
                requestPermissionLauncher.launch(Manifest.permission.CAMERA)
            }

            scanButton.visibility = View.GONE
            scanPreview.visibility = View.VISIBLE
            startCamera(scanPreview)
        }
        return view
    }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission() ) {isGranted: Boolean ->
        if(isGranted){
            Log.i("Permission: ", "Granted")
        }
        else{
            Log.i("Permission: ", "Denied")
        }
    }

    private fun startCamera(scanPreview:PreviewView){
        processingBarcode.set(false)

        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())
        cameraProviderFuture.addListener({
            val cameraProvider:ProcessCameraProvider = cameraProviderFuture.get()
            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(
                    scanPreview.surfaceProvider
                )}

            // Setup the ImageAnalyzer for ImageAnalysis
            val imageAnalysis = ImageAnalysis.Builder()
                .build()
                .also {
                    it.setAnalyzer(cameraExecutor, BarcodeScanner {barcode ->
                        if (processingBarcode.compareAndSet(false, true)){
                            searchBarcode(barcode)
                        }
                    })
                }

            // select rear camera
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageAnalysis)
            } catch (e: Exception) {
                Log.e("PreviewUseCase", "Binding failed!", e)
            }
        }, ContextCompat.getMainExecutor(requireContext()))
    }

    private fun searchBarcode(barcode: String) {
        Log.i("Barcode: ", barcode)

        // check if barcode is a shelf label and pull up the matching shelf
        var shelving: Shelving = Shelving("0")
        var shelf: Shelf = shelving.getShelfByBarcode(barcode)

        // if shelf id is -1, the shelf does not exist. Check if UPC instead
        if(shelf.id == -1) {
            var inventory: Inventory = Inventory("0")
            var item: Item = inventory.getItemByBarcode(barcode)
        }
        else {
            var inventory: Inventory = Inventory("0")
            var item: Item = inventory.getItemByBarcode(barcode)
        }
        // if no matching item, prompt user to create a new item or pick an alias
    }

}