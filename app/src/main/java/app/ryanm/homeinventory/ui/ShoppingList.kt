package app.ryanm.homeinventory.ui

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import app.ryanm.homeinventory.R
import app.ryanm.homeinventory.inventory.Inventory
import app.ryanm.homeinventory.inventory.Item
import app.ryanm.homeinventory.network.Network
import kotlinx.coroutines.launch

class ShoppingList : Fragment() {
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
        val view = inflater.inflate(R.layout.fragment_shopping_list, container, false)

        lifecycleScope.launch {
            val inventory = Inventory()

            val items = inventory.getItems(network.getServer(), network.getUser())

            for(item in items) {
                if(item.quantity < item.minimum) {

                }
            }
        }

        return view
    }
}