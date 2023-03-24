package app.ryanm.homeinventory.ui

import android.Manifest
import android.content.Context
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
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import app.ryanm.homeinventory.R
import app.ryanm.homeinventory.inventory.Inventory
import app.ryanm.homeinventory.inventory.Shelf
import app.ryanm.homeinventory.inventory.Shelving
import app.ryanm.homeinventory.network.Network
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicBoolean

typealias BarcodeListener = (barcode: String) -> Unit

class ScanFragment : Fragment() {
    private var processingBarcode = AtomicBoolean(false)
    private lateinit var cameraExecutor: ExecutorService
    private lateinit var network: Network
    private lateinit var fragComm: IFragComm

    override fun onAttach(context: Context) {
        super.onAttach(context)
        network = context as Network
        fragComm = context as IFragComm
    }

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
        lifecycleScope.launch (context = Dispatchers.IO) {
            // check if barcode is a shelf label and pull up the matching shelf
            val shelving = Shelving()
            val shelf: Shelf =
                shelving.getShelfByBarcode(barcode, network.getServer(), network.getUser())

            if (shelf.id > -1) {
                // open shelf fragment using shelf id
                val navController = parentFragmentManager.primaryNavigationFragment?.findNavController()
                navController?.navigate(R.id.action_scanFragment_to_shelfFragment)
                fragComm.setShelfFragment(shelf)
            }
            // if shelf id is -1, the shelf does not exist. Check if UPC instead
            else if (shelf.id == -1) {
                val inventory = Inventory()
                val item =
                    inventory.getItemByBarcode(barcode, network.getServer(), network.getUser())

                if (item.id > -1) {
                    // Open ItemFragment
                    val navController = parentFragmentManager.primaryNavigationFragment?.findNavController()
                    navController?.navigate(R.id.action_scanFragment_to_itemFragment)
                    fragComm.setItemFragment(item)
                } else {
                    // Open NewItemFragment
                }
            }
        }
    }

}