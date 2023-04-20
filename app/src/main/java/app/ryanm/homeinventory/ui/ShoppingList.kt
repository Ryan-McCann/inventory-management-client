package app.ryanm.homeinventory.ui

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.LinearLayout
import android.widget.TextView
import androidx.lifecycle.lifecycleScope
import app.ryanm.homeinventory.R
import app.ryanm.homeinventory.inventory.Inventory
import app.ryanm.homeinventory.inventory.ShoppingListItem
import app.ryanm.homeinventory.network.Network
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import org.json.JSONTokener
import java.io.File

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

        val shoppingListItems: MutableMap<String, ShoppingListItem> = readListFromJSON()

        lifecycleScope.launch {
            if(network.getServer().connected() && network.getUser().loggedIn) {
                val inventory = Inventory()

                val items = inventory.getItems(network.getServer(), network.getUser())

                for(item in items) {
                    if(item.quantity < item.minimum && item.quantity < item.maximum) {
                        if(shoppingListItems.containsKey(item.description)) {
                            if(shoppingListItems[item.description]!!.checked) {
                                if(shoppingListItems[item.description]!!.quantity != (item.maximum-item.quantity)) {
                                    shoppingListItems[item.description]!!.quantity = (item.maximum-item.quantity)
                                    shoppingListItems[item.description]!!.checked = false
                                }
                            } else {
                                shoppingListItems[item.description] =
                                    ShoppingListItem(item.id, item.description, (item.maximum-item.quantity), false)
                            }
                        } else {
                            shoppingListItems[item.description] =
                                ShoppingListItem(item.id, item.description, (item.maximum-item.quantity), false)
                        }
                    } else {
                        if(shoppingListItems.containsKey(item.description)) {
                            shoppingListItems.remove(item.description)
                        }
                    }
                }
            }

            if(shoppingListItems.isNotEmpty())
                writeListToJSON(shoppingListItems)

            for(shoppingListItem in shoppingListItems) {
                val layout = view.findViewById<LinearLayout>(R.id.shoppingListScrollLayout)
                val row = inflater.inflate(R.layout.shopping_list_row, container, false)

                val shoppingItemView = row.findViewById<TextView>(R.id.shoppingItemView)
                val shoppingQuantityView = row.findViewById<TextView>(R.id.shoppingQuantityView)
                val shoppingCheckBox = row.findViewById<CheckBox>(R.id.shoppingCheckBox)

                shoppingItemView.text = shoppingListItem.value.description
                shoppingQuantityView.text = shoppingListItem.value.quantity.toString()
                shoppingCheckBox.isChecked = shoppingListItem.value.checked

                shoppingCheckBox.setOnCheckedChangeListener { _, checked ->
                    shoppingListItem.value.checked = checked

                    writeListToJSON(shoppingListItems)
                }

                layout.addView(row)
            }
        }

        return view
    }

    private fun readListFromJSON(): MutableMap<String, ShoppingListItem> {
        val shoppingListItems:MutableMap<String, ShoppingListItem> = mutableMapOf()

        val jsonString = File(requireActivity().filesDir.toString()+"shopping_list.json").readText()

        val shoppingListJSON = JSONTokener(jsonString).nextValue() as JSONArray

        for(i in 0 until shoppingListJSON.length()) {
            val itemJSON = shoppingListJSON.getJSONObject(i)
            val id = itemJSON.getInt("id")
            val description = itemJSON.getString("description")
            val quantity = itemJSON.getInt("quantity")
            val checked = itemJSON.getBoolean("checked")

            shoppingListItems[description] = ShoppingListItem(id, description, quantity, checked)

        }

        return shoppingListItems
    }

    private fun writeListToJSON(shoppingListItems: MutableMap<String, ShoppingListItem>) {
        val shoppingListJSON = JSONArray()

        for(shoppingListItem in shoppingListItems) {
            val listItemJSON = JSONObject()
            listItemJSON.put("id", shoppingListItem.value.id)
            listItemJSON.put("description", shoppingListItem.value.description)
            listItemJSON.put("quantity", shoppingListItem.value.quantity)
            listItemJSON.put("checked", shoppingListItem.value.checked)
            shoppingListJSON.put(listItemJSON)
        }

        File(requireActivity().filesDir.toString()+"shopping_list.json").writeText(shoppingListJSON.toString())
    }
}