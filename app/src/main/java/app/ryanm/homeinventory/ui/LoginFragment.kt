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
import app.ryanm.homeinventory.R
import app.ryanm.homeinventory.network.Network
import kotlinx.coroutines.launch

class LoginFragment : Fragment() {
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
        val view = inflater.inflate(R.layout.fragment_login, container, false)

        val loginButton = view.findViewById<Button>(R.id.loginButton)
        val registerLinkView = view.findViewById<TextView>(R.id.registerLinkView)

        loginButton.setOnClickListener {
            val serverEdit = view.findViewById<EditText>(R.id.editServer)
            val emailEdit = view.findViewById<EditText>(R.id.editEmail)
            val passEdit = view.findViewById<EditText>(R.id.editPassword)

            var url = serverEdit.text.toString()

            if( !(url.startsWith("https://") || url.startsWith("http://")))
                url = "https://$url"

            lifecycleScope.launch{
                val loginErrorView = view.findViewById<TextView>(R.id.loginErrorView)

                if(network.getServer().connect(url)) {
                    when(val result = network.getUser().login(emailEdit.text.toString(), passEdit.text.toString(), network.getServer())) {
                        "user-disabled" -> {
                            loginErrorView.visibility = View.VISIBLE
                            loginErrorView.text = "Error: User is disabled."
                        }
                        "invalid-user" -> {
                            loginErrorView.visibility = View.VISIBLE
                            loginErrorView.text = "Error: Invalid user."
                        }
                        "invalid-password" -> {
                            loginErrorView.visibility = View.VISIBLE
                            loginErrorView.text = "Error: Incorrect password."
                        }
                        else -> {
                            if (network.getUser().loggedIn) {
                                val sharedPrefs = requireActivity().getPreferences(Context.MODE_PRIVATE)
                                with(sharedPrefs.edit()) {
                                    putString(getString(R.string.server), network.getServer().url)
                                    putString(getString(R.string.email), network.getUser().username)
                                    putString(getString(R.string.token), network.getUser().token)
                                    apply()
                                }

                                view.findNavController().navigate(R.id.action_loginFragment_to_scanFragment)
                            }
                        }
                    }
                } else {
                    loginErrorView.visibility = View.VISIBLE
                    loginErrorView.text = "Error: Invalid Server."
                }
            }
        }

        registerLinkView.setOnClickListener {

            view.findNavController().navigate(R.id.action_loginFragment_to_registerFragment)
        }

        return view
    }
}