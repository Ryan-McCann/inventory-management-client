package app.ryanm.homeinventory.ui

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.os.bundleOf
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import app.ryanm.homeinventory.R
import app.ryanm.homeinventory.inventory.Inventory
import app.ryanm.homeinventory.network.Network
import kotlinx.coroutines.launch

class InventoryFragment : Fragment() {
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
        val view = inflater.inflate(R.layout.fragment_inventory, container, false)
        val inventoryScrollLayout = view.findViewById<LinearLayout>(R.id.inventoryScrollLayout)

        lifecycleScope.launch {
            val inventory = Inventory()
            val items = inventory.getItems(network.getServer(), network.getUser())

            for(item in items) {
                val layout = inflater.inflate(R.layout.inventory_row, null)
                val itemTextView = layout.findViewById<TextView>(R.id.inventoryItemView)
                itemTextView.text = item.description
                itemTextView.isClickable = true
                itemTextView.setOnClickListener {
                    lifecycleScope.launch {
                        val itemDeep = inventory.getItemById(item.id, network.getServer(), network.getUser())
                        val navController =
                            parentFragmentManager.primaryNavigationFragment?.findNavController()
                        val bundle = bundleOf("item" to itemDeep)

                        navController?.navigate(
                            R.id.action_inventoryFragment_to_itemFragment,
                            bundle
                        )
                    }
                }
                inventoryScrollLayout.addView(layout)
            }
        }

        return view
    }
}