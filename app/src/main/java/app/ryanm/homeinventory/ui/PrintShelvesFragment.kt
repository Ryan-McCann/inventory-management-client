package app.ryanm.homeinventory.ui

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.allViews
import androidx.lifecycle.lifecycleScope
import app.ryanm.homeinventory.Barcode
import app.ryanm.homeinventory.R
import app.ryanm.homeinventory.inventory.Shelving
import app.ryanm.homeinventory.network.Network
import com.google.zxing.oned.Code128Writer
import kotlinx.coroutines.launch

class PrintShelvesFragment : Fragment() {
    private lateinit var network: Network

    override fun onAttach(context: Context) {
        super.onAttach(context)
        network = context as Network
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_print_shelves, container, false)

        val shelving = Shelving()

        var checkedBarcodeState:MutableMap<String, Boolean> = mutableMapOf()

        lifecycleScope.launch {
            val shelves = shelving.getShelves(network.getServer(), network.getUser())

            for(shelf in shelves) {
                val printShelfLayout = view.findViewById<LinearLayout>(R.id.printShelvesLayout)
                val shelfRow = inflater.inflate(R.layout.print_shelf_row, null)
                val printShelfLabel = shelfRow.findViewById<TextView>(R.id.printShelfLabelView)

                checkedBarcodeState[shelf.barcode] = false

                val printShelfCheckBox = shelfRow.findViewById<CheckBox>(R.id.printShelfCheckBox)
                printShelfCheckBox.setOnCheckedChangeListener { _, b ->
                    checkedBarcodeState[shelf.barcode] = b
                }

                printShelfLabel.text = shelf.label

                printShelfLayout.addView(shelfRow)
            }
        }

        val templateSpinner = view.findViewById<Spinner>(R.id.templateSpinner)

        val barcodes = ArrayList<Barcode>()

        val printButton = view.findViewById<Button>(R.id.printButton)
        printButton.setOnClickListener {
            for (barcodeState in checkedBarcodeState) {
                if (barcodeState.value) {
                    barcodes.add(Barcode(barcodeState.key))
                }
            }

            for(barcode in barcodes) {

            }
        }

        return view
    }
}