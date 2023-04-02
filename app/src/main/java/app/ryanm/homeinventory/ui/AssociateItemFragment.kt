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

class AssociateItemFragment : Fragment() {
    private lateinit var network: Network

    override fun onAttach(context: Context) {
        super.onAttach(context)
        network = context as Network
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_associate_item, container, false)

        val barcode = arguments?.getString("barcode").toString()

        val assocItemLayout = view.findViewById<LinearLayout>(R.id.assocItemLayout)
        val createItemLink = view.findViewById<TextView>(R.id.createItemTextView)

        lifecycleScope.launch {
            val inventory = Inventory()
            val items = inventory.getItems(network.getServer(), network.getUser())

            for(item in items) {
                val itemDescriptView = TextView(activity)
                itemDescriptView.text = item.description
                itemDescriptView.isClickable = true
                itemDescriptView.setOnClickListener {
                    lifecycleScope.launch {
                        inventory.associateItem(item, barcode, network.getServer(), network.getUser())
                        val navController = parentFragmentManager.primaryNavigationFragment?.findNavController()

                        navController?.navigate(R.id.action_associateItemFragment_to_inventoryFragment)
                    }
                }
                assocItemLayout.addView(itemDescriptView)
            }
        }
        createItemLink.setOnClickListener {
            val bundle = bundleOf("barcode" to barcode)
            val navController = parentFragmentManager.primaryNavigationFragment?.findNavController()

            navController?.navigate(R.id.action_associateItemFragment_to_newItemFragment, bundle)
        }

        return view
    }
}