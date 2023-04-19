package app.ryanm.homeinventory.ui

import android.content.Context
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.os.bundleOf
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import app.ryanm.homeinventory.R
import app.ryanm.homeinventory.inventory.Shelving
import app.ryanm.homeinventory.network.Network
import kotlinx.coroutines.launch

class ShelvesFragment : Fragment(), MenuProvider {
    private lateinit var network: Network

    override fun onAttach(context: Context) {
        super.onAttach(context)
        network = context as Network
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val menuHost: MenuHost = requireActivity()
        menuHost.addMenuProvider(this, viewLifecycleOwner, Lifecycle.State.RESUMED)

        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_shelves, container, false)

        val shelvesScrollLayout = view.findViewById<LinearLayout>(R.id.shelvesScrollLayout)

        // Load existing shelves from server
        lifecycleScope.launch {
            val shelving = Shelving()
            val shelves = shelving.getShelves(network.getServer(), network.getUser())

            for(shelf in shelves) {
                val shelfRow = inflater.inflate(R.layout.shelf_row, container, false)
                val shelfLabel = shelfRow.findViewById<TextView>(R.id.shelfRowLabel)
                val deleteShelfButton = shelfRow.findViewById<TextView>(R.id.deleteShelfButton)

                shelfLabel.text = shelf.label

                shelfRow.setOnClickListener {
                    val navController = parentFragmentManager.primaryNavigationFragment?.findNavController()
                    val bundle = bundleOf("shelf" to shelf)
                    navController?.navigate(R.id.action_shelvesFragment_to_shelfFragment, bundle)
                }

                deleteShelfButton.setOnClickListener{
                    lifecycleScope.launch {
                        shelving.deleteShelf(shelf, network.getServer(), network.getUser())
                        val navController = parentFragmentManager.primaryNavigationFragment?.findNavController()
                        navController?.navigate(R.id.shelvesFragment)
                    }
                }

                shelvesScrollLayout.addView(shelfRow)
            }
        }

        // Set onClick handler for add shelf button
        val addShelfButton = view.findViewById<Button>(R.id.addShelfButton)
        addShelfButton.setOnClickListener {
            val editShelfLabel = view.findViewById<EditText>(R.id.editShelfLabel)
            if(editShelfLabel.text.isNotEmpty() && editShelfLabel.text.isNotBlank()) {
                lifecycleScope.launch {
                    val shelving = Shelving()
                    shelving.createShelf(editShelfLabel.text.toString(), network.getServer(), network.getUser())

                    val navController = parentFragmentManager.primaryNavigationFragment?.findNavController()
                    navController?.navigate(R.id.shelvesFragment)
                }
            }
        }

        return view
    }

    override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
        menuInflater.inflate(R.menu.options, menu)
    }

    override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
        if(menuItem.title == getString(R.string.print_barcodes)) {
            val navController = parentFragmentManager.primaryNavigationFragment?.findNavController()

            navController?.navigate(R.id.action_shelvesFragment_to_printShelvesFragment)
        }
        return false
    }
}