package app.ryanm.homeinventory.ui

import android.content.Context
import android.content.res.Resources
import android.os.Bundle
import android.print.PrintAttributes
import android.print.PrintManager
import android.print.pdf.PrintedPdfDocument
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import app.ryanm.homeinventory.PdfPrintAdapter
import app.ryanm.homeinventory.barcode.Barcode
import app.ryanm.homeinventory.R
import app.ryanm.homeinventory.barcode.LabelTemplate
import app.ryanm.homeinventory.inventory.Shelving
import app.ryanm.homeinventory.network.Network
import kotlinx.coroutines.launch
import org.xmlpull.v1.XmlPullParserFactory
import java.io.File
import java.io.FileOutputStream

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

        var checkedBarcodeState:MutableMap<Pair<String, String>, Boolean> = mutableMapOf()

        lifecycleScope.launch {
            val shelves = shelving.getShelves(network.getServer(), network.getUser())

            for(shelf in shelves) {
                val printShelfLayout = view.findViewById<LinearLayout>(R.id.printShelvesLayout)
                val shelfRow = inflater.inflate(R.layout.print_shelf_row, null)
                val printShelfLabel = shelfRow.findViewById<TextView>(R.id.printShelfLabelView)

                checkedBarcodeState[Pair(shelf.barcode, shelf.label)] = false

                val printShelfCheckBox = shelfRow.findViewById<CheckBox>(R.id.printShelfCheckBox)
                printShelfCheckBox.setOnCheckedChangeListener { _, b ->
                    checkedBarcodeState[Pair(shelf.barcode, shelf.label)] = b
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
                    barcodes.add(Barcode(barcodeState.key.first, barcodeState.key.second))
                }
            }

            val image = ImageView(requireContext())
            image.setImageBitmap(barcodes[0].generate(200, 200, ContextCompat.getColor(requireContext(), R.color.black), ContextCompat.getColor(requireContext(), R.color.white)))
            val printShelfLayout = view.findViewById<LinearLayout>(R.id.printShelvesLayout)
            printShelfLayout.addView(image)

            val xmlFile = requireContext().assets.open("templates/template_94200.xml")
            val xmlParser = XmlPullParserFactory.newInstance().newPullParser()
            xmlParser.setInput(xmlFile, null)
            val label = LabelTemplate(xmlParser)

            val document = label.generateLabels(barcodes, requireContext().getColor(R.color.black), requireContext().getColor(R.color.white))

            val printManager = requireContext().getSystemService(Context.PRINT_SERVICE) as PrintManager
            val jobName = "${requireContext().getString(R.string.app_name)} Document"

            printManager.print(jobName, PdfPrintAdapter(requireContext(), document), null)

            barcodes.clear()
        }

        return view
    }
}