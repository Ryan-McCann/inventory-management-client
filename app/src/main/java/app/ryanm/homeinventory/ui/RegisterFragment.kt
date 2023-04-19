package app.ryanm.homeinventory.ui

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import app.ryanm.homeinventory.R
import app.ryanm.homeinventory.network.Network
import kotlinx.coroutines.launch

class RegisterFragment : Fragment() {
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
        val view = inflater.inflate(R.layout.fragment_register, container, false)

        val loginLinkView = view.findViewById<TextView>(R.id.loginLinkView)
        val registerButton = view.findViewById<Button>(R.id.registerButton)
        val editServer = view.findViewById<EditText>(R.id.editServer2)
        val editEmail = view.findViewById<EditText>(R.id.editEmail2)
        val editPassword = view.findViewById<EditText>(R.id.editPassword2)
        val editConfirm = view.findViewById<EditText>(R.id.editConfirm)

        val registerErrorView = view.findViewById<TextView>(R.id.registerErrorView)

        registerButton.setOnClickListener {
            var url = editServer.text.toString()
            val email = editEmail.text.toString()
            val password = editPassword.text.toString()
            val confirm = editConfirm.text.toString()

            if( !(url.startsWith("https://") || url.startsWith("http://")))
                url = "https://$url"

            if(password == confirm) {
                lifecycleScope.launch {
                    if (network.getServer().connect(url)) {
                        when (network.getUser()
                            .register(email, password, network.getServer())) {
                            "success" -> {
                                val navController =
                                    parentFragmentManager.primaryNavigationFragment?.findNavController()
                                navController?.navigate(R.id.action_registerFragment_to_loginFragment)
                            }
                            "user" -> {
                                registerErrorView.text = "Error: User already exists."
                            }
                        }
                    } else {
                        registerErrorView.text = "Error: Invalid server."
                    }
                }
            } else {
                registerErrorView.text = "Error: Passwords don't match."
            }
        }

        loginLinkView.setOnClickListener {
            view.findNavController().navigate(R.id.action_registerFragment_to_loginFragment)
        }

        return view
    }
}