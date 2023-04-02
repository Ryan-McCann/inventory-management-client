package app.ryanm.homeinventory.ui

import android.content.Context
import android.os.Bundle
import android.util.TypedValue
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.LinearLayout.HORIZONTAL
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.os.bundleOf
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import app.ryanm.homeinventory.R
import app.ryanm.homeinventory.inventory.Inventory
import app.ryanm.homeinventory.inventory.Item
import app.ryanm.homeinventory.inventory.Shelving
import app.ryanm.homeinventory.network.Network
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ItemFragment : Fragment() {
    private lateinit var view: View
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
        view = inflater.inflate(R.layout.fragment_item, container, false)

        val editDescription = view.findViewById<EditText>(R.id.editDescription)
        val editMinimum = view.findViewById<EditText>(R.id.editMinimum)
        val editMaximum = view.findViewById<EditText>(R.id.editMaximum)
        val quantityView = view.findViewById<TextView>(R.id.quantityView)

        val item: Item = arguments?.getParcelable("item")!!

        editDescription.setText(item.description)
        editMinimum.setText(item.minimum.toString())
        editMaximum.setText(item.maximum.toString())
        quantityView.text = item.quantity.toString()

        for(barcode in item.barcodes) {
            val barcodeLayout = view.findViewById<LinearLayout>(R.id.barcodeLinearLayout)
            val layout = inflater.inflate(R.layout.inventory_barcode_row, null) as ConstraintLayout

            val barcodeView = layout.findViewById<TextView>(R.id.inventoryBarcodeView)
            barcodeView.text = barcode

            val barcodeDeleteButton = layout.findViewById<Button>(R.id.inventoryBarcodeDeleteButton)
            barcodeDeleteButton.setOnClickListener {
                lifecycleScope.launch {
                    val inventory = Inventory()
                    inventory.removeAlias(barcode, network.getServer(), network.getUser())

                    val newItem = inventory.getItemById(item.id, network.getServer(), network.getUser())


                    val bundle = bundleOf("item" to newItem)
                    val navController = parentFragmentManager.primaryNavigationFragment?.findNavController()
                    navController?.popBackStack()
                    navController?.navigate(R.id.itemFragment, bundle)
                }
            }

            barcodeLayout.addView(layout)
        }

        lifecycleScope.launch {
            val shelfRows = ArrayList<Pair<String, Int>>()

            val shelving = Shelving()

            for(shelfId in item.shelfIds) {
                val shelf = shelving.getShelfById(shelfId, network.getServer(), network.getUser())
                val shelfQuantity = shelving.getShelfItemQuantity(shelf, item, network.getServer(), network.getUser())
                shelfRows.add(Pair(shelf.label, shelfQuantity))
            }

            for(row in shelfRows) {
                val shelfLayout = view.findViewById<LinearLayout>(R.id.shelfLinearLayout)
                val inventoryShelfRow = inflater.inflate(R.layout.inventory_shelf_row, null)
                inventoryShelfRow.isClickable = true

                val itemShelfView = inventoryShelfRow.findViewById<TextView>(R.id.itemShelfView)
                itemShelfView.text = row.first

                val itemShelfQuantView = inventoryShelfRow.findViewById<TextView>(R.id.itemShelfQuantView)
                itemShelfQuantView.text = "x${row.second}"

                inventoryShelfRow.setOnClickListener {
                    lifecycleScope.launch {
                        val shelf = shelving.getShelfByLabel(row.first, network.getServer(), network.getUser())
                        val bundle = bundleOf("shelf" to shelf)
                        val navController = parentFragmentManager.primaryNavigationFragment?.findNavController()

                        navController?.navigate(R.id.action_itemFragment_to_shelfFragment, bundle)
                    }
                }

                shelfLayout.addView(inventoryShelfRow)
            }
        }

        val updateButton = view.findViewById<Button>(R.id.updateButton)

        // On clicking update, update item on server
        updateButton.setOnClickListener{
            val inventory = Inventory()

            lifecycleScope.launch (context = Dispatchers.IO) {
                item.description = editDescription.text.toString()
                item.minimum = editMinimum.text.toString().toInt()
                item.maximum = editMaximum.text.toString().toInt()

                inventory.updateItem(item, network.getServer(), network.getUser())
            }
        }

        val deleteItemButton = view.findViewById<Button>(R.id.itemDeleteButton)

        deleteItemButton.setOnClickListener{
            val inventory = Inventory()

            lifecycleScope.launch {
                inventory.deleteItem(item, network.getServer(), network.getUser())
                val navController = parentFragmentManager.primaryNavigationFragment?.findNavController()
                navController?.navigate(R.id.inventoryFragment)
            }
        }

        return view
    }
}