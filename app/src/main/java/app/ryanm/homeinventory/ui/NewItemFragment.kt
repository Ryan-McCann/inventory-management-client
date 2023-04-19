package app.ryanm.homeinventory.ui

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.core.os.bundleOf
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import app.ryanm.homeinventory.R
import app.ryanm.homeinventory.inventory.Inventory
import app.ryanm.homeinventory.inventory.Item
import app.ryanm.homeinventory.network.Network
import kotlinx.coroutines.launch

class NewItemFragment : Fragment() {
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
        val view = inflater.inflate(R.layout.fragment_new_item, container, false)

        val editNewDesc = view.findViewById<EditText>(R.id.editNewDesc)
        val editNewMin = view.findViewById<EditText>(R.id.editNewMin)
        val editNewMax = view.findViewById<EditText>(R.id.editNewMax)
        val editNewUPC = view.findViewById<EditText>(R.id.editNewUPC)
        val updateButton = view.findViewById<Button>(R.id.updateNewItemButton)
        val switchToAliasView = view.findViewById<TextView>(R.id.switchToAliasView)

        val barcode = arguments?.getString("barcode").toString()

        editNewUPC.setText(barcode)

        updateButton.setOnClickListener {
            lifecycleScope.launch {
                val item = Item()
                item.description = editNewDesc.text.toString()
                item.minimum = editNewMin.text.toString().toInt()
                item.maximum = editNewMax.text.toString().toInt()
                item.barcodes.add(barcode)

                val inventory = Inventory()
                inventory.createItem(item, network.getServer(), network.getUser())

                val navController = parentFragmentManager.primaryNavigationFragment?.findNavController()

                navController?.navigate(R.id.action_newItemFragment_to_inventoryFragment)
            }
        }

        switchToAliasView.setOnClickListener {
            val bundle = bundleOf("barcode" to barcode)
            val navController = parentFragmentManager.primaryNavigationFragment?.findNavController()

            navController?.navigate(R.id.action_newItemFragment_to_associateItemFragment, bundle)
        }

        return view
    }
}