package app.ryanm.homeinventory.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import app.ryanm.homeinventory.R
import app.ryanm.homeinventory.inventory.Item

class ItemFragment : Fragment() {
    private lateinit var item: Item

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_item, container, false)
    }

    fun setItem(item: Item) {
        this.item = item
    }
}