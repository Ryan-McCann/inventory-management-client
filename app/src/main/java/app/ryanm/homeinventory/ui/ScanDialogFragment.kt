package app.ryanm.homeinventory.ui

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import app.ryanm.homeinventory.R
import app.ryanm.homeinventory.network.Network
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicBoolean

class ScanDialogFragment: DialogFragment() {
    private lateinit var scanPreview: PreviewView

    private var processingBarcode = AtomicBoolean(false)

    private lateinit var network: Network

    private lateinit var cameraExecutor: ExecutorService

    private var listener: ((String)->Unit)? = null

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission() ) { isGranted: Boolean ->
        if(isGranted){
            Log.i("Permission: ", "Granted")
        }
        else{
            Log.i("Permission: ", "Denied")
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        network = context as Network

        if(ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_DENIED){
            requestPermissionLauncher.launch(Manifest.permission.CAMERA)
        }

        startCamera()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        cameraExecutor = Executors.newSingleThreadExecutor()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.dialog_scan, container, false)
        scanPreview = view.findViewById(R.id.popupScanPreview)
        return view
    }

    private fun startCamera(){
        processingBarcode.set(false)

        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())
        cameraProviderFuture.addListener({
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()
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
                            listener?.invoke(barcode)
                            dismiss()
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

    fun setOnScanListener(lambda: (String) -> Unit) {
        listener = lambda
    }
}