package app.ryanm.homeinventory.ui

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.lifecycle.lifecycleScope
import app.ryanm.homeinventory.R
import app.ryanm.homeinventory.inventory.Inventory
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
            if(network.getServer().connected() && network.getUser().loggedIn) {
                val inventory = Inventory()

                val items = inventory.getItems(network.getServer(), network.getUser())

                for(item in items) {
                    if(item.quantity < item.minimum && item.quantity < item.maximum) {
                        val layout = view.findViewById<LinearLayout>(R.id.shoppingListScrollLayout)
                        val row = inflater.inflate(R.layout.shopping_list_row, container, false)

                        val shoppingItemView = row.findViewById<TextView>(R.id.shoppingItemView)
                        val shoppingQuantityView = row.findViewById<TextView>(R.id.shoppingQuantityView)

                        shoppingItemView.text = item.description
                        shoppingQuantityView.text = (item.maximum-item.quantity).toString()

                        layout.addView(row)
                    }
                }
            }
        }

        return view
    }
}