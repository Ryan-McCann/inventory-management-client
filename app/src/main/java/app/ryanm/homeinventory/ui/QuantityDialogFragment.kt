package app.ryanm.homeinventory.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import androidx.core.text.isDigitsOnly
import androidx.fragment.app.DialogFragment
import app.ryanm.homeinventory.R

class QuantityDialogFragment: DialogFragment() {
    private var listener: ((Int)->Unit)? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.dialog_quantity, container, false)

        val editQuantityPopup = view.findViewById<EditText>(R.id.editQuantityPopup)
        val quantityPopupButton = view.findViewById<Button>(R.id.quantityPopupButton)

        quantityPopupButton.setOnClickListener{
            if(editQuantityPopup.text.isNotEmpty() && editQuantityPopup.text.isNotBlank() && editQuantityPopup.text.isDigitsOnly())
                listener?.invoke(editQuantityPopup.text.toString().toInt())
        }

        return view
    }

    fun setOnQuantityEnteredListener(lambda: (Int)->Unit) {
        listener = lambda
    }
}