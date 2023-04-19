package app.ryanm.homeinventory.ui

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.os.bundleOf
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import app.ryanm.homeinventory.R
import app.ryanm.homeinventory.inventory.Item
import app.ryanm.homeinventory.inventory.Shelf
import app.ryanm.homeinventory.inventory.Shelving
import app.ryanm.homeinventory.network.Network
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ShelfFragment : Fragment() {
    private lateinit var shelf: Shelf

    private lateinit var network: Network

    private lateinit var scanDialog: ScanDialogFragment

    override fun onAttach(context: Context) {
        super.onAttach(context)
        network = context as Network
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_shelf, container, false)

        shelf = arguments?.getParcelable("shelf")!!

        val shelfItemsScrollLayout = view.findViewById<LinearLayout>(R.id.shelfItemsScrollLayout)
        val editUPC = view.findViewById<EditText>(R.id.editUPC)
        val scanButton2 = view.findViewById<ImageButton>(R.id.scanButton2)
        val addItemButton = view.findViewById<Button>(R.id.addItemButton)

        lifecycleScope.launch {
            val shelving = Shelving()
            val items: ArrayList<Item> = shelving.getItemsByShelf(shelf, network.getServer(), network.getUser())

            for(item in items) {
                val layout = inflater.inflate(R.layout.shelf_item_row, container, false) as ConstraintLayout

                val itemView = layout.findViewById<TextView>(R.id.itemDescView)
                itemView.text = item.description

                val quantityView = layout.findViewById<TextView>(R.id.itemQuantView)
                quantityView.text = String.format(getString(R.string.quant_val), item.quantity)

                // listen for clicks to adjust quantity
                quantityView.setOnClickListener {
                    val quantityDialog = QuantityDialogFragment()
                    quantityDialog.show(childFragmentManager, "")
                    quantityDialog.setOnQuantityEnteredListener {
                        val newQuantity = it
                        if(item.quantity < newQuantity) {
                            lifecycleScope.launch {
                                shelving.addItemToShelfByBarcode(item.barcodes[0], newQuantity-item.quantity, shelf, network.getServer(), network.getUser())

                                val bundle = bundleOf("shelf" to shelf)
                                val navController = parentFragmentManager.primaryNavigationFragment?.findNavController()
                                navController?.popBackStack()
                                navController?.navigate(R.id.shelfFragment, bundle)
                            }
                        } else if(item.quantity > newQuantity) {
                            lifecycleScope.launch {
                                shelving.removeItemFromShelfByBarcode(item.barcodes[0], item.quantity-newQuantity, shelf, network.getServer(), network.getUser())

                                val bundle = bundleOf("shelf" to shelf)
                                val navController = parentFragmentManager.primaryNavigationFragment?.findNavController()
                                navController?.popBackStack()
                                navController?.navigate(R.id.shelfFragment, bundle)
                            }
                        }
                    }
                }

                // listen for clicks to move item to another shelf
                val moveButton = layout.findViewById<Button>(R.id.moveItemButton)
                moveButton.setOnClickListener {
                    var itemQuantity = 0

                    for(shelfQuantity in item.shelfQuantities)
                        if(shelfQuantity.label == shelf.label)
                            itemQuantity = shelfQuantity.itemQuantity

                    val moveDialog = MoveDialogFragment(itemQuantity)
                    moveDialog.show(childFragmentManager, "")
                    moveDialog.setOnShelfEnteredListener { shelfId, quantity ->
                        lifecycleScope.launch {
                            val shelf2 = shelving.getShelfById(shelfId, network.getServer(), network.getUser())
                            shelving.removeItemFromShelfByBarcode(item.barcodes[0], quantity, shelf, network.getServer(), network.getUser())
                            shelving.addItemToShelfByBarcode(item.barcodes[0], quantity, shelf2, network.getServer(), network.getUser())

                            val bundle = bundleOf("shelf" to shelf)
                            val navController = parentFragmentManager.primaryNavigationFragment?.findNavController()
                            navController?.popBackStack()
                            navController?.navigate(R.id.shelfFragment, bundle)
                        }
                    }
                }

                // listen for clicks to remove item from shelf
                val removeButton = layout.findViewById<Button>(R.id.removeItemButton)
                removeButton.setOnClickListener {
                    lifecycleScope.launch {
                        shelving.removeItemFromShelfByBarcode(item.barcodes[0], item.quantity, shelf, network.getServer(), network.getUser())

                        val bundle = bundleOf("shelf" to shelf)
                        val navController = parentFragmentManager.primaryNavigationFragment?.findNavController()
                        navController?.popBackStack()
                        navController?.navigate(R.id.shelfFragment, bundle)
                    }
                }

                shelfItemsScrollLayout.addView(layout)
            }
        }

        scanButton2.setOnClickListener {
            // load popup showing scan preview, scan upc here
            scanDialog = ScanDialogFragment()
            scanDialog.show(childFragmentManager, "")
            scanDialog.setOnScanListener {
                searchBarcode(it)
            }
        }

        addItemButton.setOnClickListener {
            val barcode = editUPC.text.toString()

            lifecycleScope.launch {
                val shelving = Shelving()
                shelving.addItemToShelfByBarcode(barcode,1, shelf, network.getServer(), network.getUser())
            }
        }

        return view
    }

    private fun searchBarcode(barcode: String) {
        lifecycleScope.launch (context = Dispatchers.Main) {
            val shelving = Shelving()
            shelving.addItemToShelfByBarcode(barcode, 1, shelf, network.getServer(), network.getUser())

            val bundle = bundleOf("shelf" to shelf)

            val navController = parentFragmentManager.primaryNavigationFragment?.findNavController()
            val currentId = navController?.currentDestination?.id
            navController?.popBackStack(currentId!!, true)
            navController?.navigate(R.id.shelfFragment, bundle)
        }
    }
}