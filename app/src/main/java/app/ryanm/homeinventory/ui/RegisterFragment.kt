package app.ryanm.homeinventory.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.navigation.findNavController
import app.ryanm.homeinventory.R

class RegisterFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_register, container, false)

        val loginLinkView = view.findViewById<TextView>(R.id.loginLinkView)

        loginLinkView.setOnClickListener {
            view.findNavController().navigate(R.id.action_registerFragment_to_loginFragment)
        }

        return view
    }

    fun registerError(error: String) {
        when(error) {
            "invalid-server" -> {

            }
            "invalid-user" -> {

            }
        }
    }
}