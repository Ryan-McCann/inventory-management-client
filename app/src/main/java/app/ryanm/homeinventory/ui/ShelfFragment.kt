package app.ryanm.homeinventory.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import app.ryanm.homeinventory.R
import app.ryanm.homeinventory.inventory.Shelf

class ShelfFragment : Fragment() {
    private lateinit var shelf: Shelf
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_shelf, container, false)
    }

    fun setShelf(shelf: Shelf) {
        this.shelf = shelf
    }
}