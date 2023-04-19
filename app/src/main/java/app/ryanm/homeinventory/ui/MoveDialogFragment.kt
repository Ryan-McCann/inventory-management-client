package app.ryanm.homeinventory.ui

import android.app.ActionBar.LayoutParams
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.lifecycleScope
import app.ryanm.homeinventory.R
import app.ryanm.homeinventory.inventory.Shelving
import app.ryanm.homeinventory.network.Network
import kotlinx.coroutines.launch

class MoveDialogFragment(private val maxQuantity: Int): DialogFragment() {
    private var listener: ((Int, Int)->Unit)? = null
    private lateinit var network: Network

    override fun onAttach(context: Context) {
        super.onAttach(context)
        network = context as Network
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.dialog_move, container, false)

        val selectShelfSpinner = view.findViewById<Spinner>(R.id.selectShelfSpinner)

        val selectQuantitySpinner = view.findViewById<Spinner>(R.id.selectQuantitySpinner)

        val selectShelfButton = view.findViewById<Button>(R.id.selectShelfButton)

        val itemQuantities = ArrayList<Int>()

        for(i in 0 .. maxQuantity)
            itemQuantities.add(i)

        val quantityAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, itemQuantities)
        selectQuantitySpinner.adapter = quantityAdapter

        val shelfIds: MutableMap<String, Int> = mutableMapOf()

        val shelfLabels = ArrayList<String>()

        lifecycleScope.launch {
            val shelving = Shelving()
            val shelves = shelving.getShelves(network.getServer(), network.getUser())

            for(shelf in shelves) {
                shelfIds[shelf.label] = shelf.id
                shelfLabels.add(shelf.label)
            }

            val shelfLabelAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, shelfLabels)
            selectShelfSpinner.adapter = shelfLabelAdapter
        }

        selectShelfButton.setOnClickListener {
            var selectedShelfId = -1
            var quantity = 0

            if(selectShelfSpinner.selectedItem != null)
                selectedShelfId = shelfIds[selectShelfSpinner.selectedItem.toString()]!!

            if(selectQuantitySpinner.selectedItem != null)
                quantity = selectQuantitySpinner.selectedItem.toString().toInt()

            if(selectedShelfId != -1)
                listener?.invoke(selectedShelfId, quantity)
        }

        return view
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
    }

    fun setOnShelfEnteredListener(lambda: (Int,Int)->Unit) {
        listener = lambda
    }
}