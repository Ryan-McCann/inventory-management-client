package app.ryanm.homeinventory

import android.content.Context
import android.os.Bundle
import android.view.Menu
import android.widget.LinearLayout
import android.widget.TextView
import com.google.android.material.navigation.NavigationView
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import androidx.drawerlayout.widget.DrawerLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import app.ryanm.homeinventory.databinding.ActivityMainBinding
import app.ryanm.homeinventory.network.Network
import app.ryanm.homeinventory.network.Server
import app.ryanm.homeinventory.network.User
import app.ryanm.homeinventory.ui.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity(), Network {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding

    private lateinit var navMenu: Menu

    private var server = Server()
    private var user = User()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.appBarMain.toolbar)

        val drawerLayout: DrawerLayout = binding.drawerLayout
        val navView: NavigationView = binding.navView
        val navController = findNavController(R.id.nav_host_fragment_content_main)

        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.loginFragment, R.id.scanFragment, R.id.inventoryFragment, R.id.shelvesFragment, R.id.shoppingList
            ), drawerLayout
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        navMenu = navView.menu

        navView.setCheckedItem(R.id.scanFragment)

        val loginLayout = navView.getHeaderView(0).findViewById<LinearLayout>(R.id.loginLayout)

        // When clicking header, change menu to login/sign-out options and hide other menu items
        loginLayout.setOnClickListener {
            val expandView = findViewById<TextView>(R.id.expandView)

            // clicking to expand login menu
            if(expandView.text.toString() == getString(R.string.down_arrow)) {
                expandView.text = getString(R.string.up_arrow)

                // if user is signed in
                if(user.loggedIn) {
                    navMenu.findItem(R.id.loginFragment).isVisible = false
                    navMenu.findItem(R.id.signoutLink).isVisible = true
                } else { // if user isn't signed in
                    navMenu.findItem(R.id.loginFragment).isVisible = true
                    navMenu.findItem(R.id.signoutLink).isVisible = false
                }

                navMenu.findItem(R.id.scanFragment).isVisible = false
                navMenu.findItem(R.id.inventoryFragment).isVisible = false
                navMenu.findItem(R.id.shelvesFragment).isVisible = false
                navMenu.findItem(R.id.shoppingList).isVisible = false
            } else { // clicking to retract login menu
                expandView.text = getString(R.string.down_arrow)

                navMenu.findItem(R.id.loginFragment).isVisible = false
                navMenu.findItem(R.id.signoutLink).isVisible = false

                // if user is signed in
                if(user.loggedIn) {
                    navMenu.findItem(R.id.scanFragment).isVisible = true
                    navMenu.findItem(R.id.inventoryFragment).isVisible = true
                    navMenu.findItem(R.id.shelvesFragment).isVisible = true
                }
                navMenu.findItem(R.id.shoppingList).isVisible = true
            }
        }

        // On click, sign out user and set menu items to default not signed in view
        navMenu.findItem(R.id.signoutLink).setOnMenuItemClickListener {

            lifecycleScope.launch {
                user.signout(server)
            }

            val expandView = findViewById<TextView>(R.id.expandView)

            user.token = ""
            user.username = ""
            server.url = ""

            expandView.text = getString(R.string.down_arrow)

            navMenu.findItem(R.id.loginFragment).isVisible = false
            navMenu.findItem(R.id.signoutLink).isVisible = false

            navMenu.findItem(R.id.scanFragment).isVisible = false
            navMenu.findItem(R.id.inventoryFragment).isVisible = false
            navMenu.findItem(R.id.shelvesFragment).isVisible = false

            navMenu.findItem(R.id.shoppingList).isVisible = true

            val userView = findViewById<TextView>(R.id.userView)
            userView.text = getString(R.string.signed_out)

            navController.navigate(R.id.shoppingList)

            return@setOnMenuItemClickListener true
        }

        val sharedPrefs = getPreferences(Context.MODE_PRIVATE)
        if(sharedPrefs.contains(getString(R.string.server))
            && sharedPrefs.contains(getString(R.string.email))
            && sharedPrefs.contains(getString(R.string.token))) {
            val serverString = sharedPrefs.getString(getString(R.string.server), null)
            val email = sharedPrefs.getString(getString(R.string.email), null)
            val token = sharedPrefs.getString(getString(R.string.token), null)

            if (serverString != null && email != null && token != null) {
                server.url = serverString
                user.username = email
                user.token = token
            }
        }

        lifecycleScope.launch (context = Dispatchers.Main) {
            if(server.connected()) {
                user.login(server)
            }

            if(server.connected() && user.loggedIn(server) ) {
                navMenu.findItem(R.id.scanFragment).isVisible = true
                navMenu.findItem(R.id.inventoryFragment).isVisible = true
                navMenu.findItem(R.id.shelvesFragment).isVisible = true

                val userView = findViewById<TextView>(R.id.userView)
                userView.text = user.username
            } else {
                navController.navigate(R.id.action_scanFragment_to_loginFragment)
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    override fun getServer(): Server {
        return server
    }

    override fun getUser(): User {
        return user
    }

    override fun login(email: String, password: String, url: String) {
        lifecycleScope.launch (context = Dispatchers.Main) {
            if(server.connect(url)) {
                when (user.login(email, password, server)) {
                    "invalid-user" -> {
                        val loginFragment =
                            supportFragmentManager.findFragmentById(R.id.loginFragment) as LoginFragment
                        loginFragment.loginError("invalid-user")
                    }
                    "invalid-password" -> {
                        val loginFragment =
                            supportFragmentManager.findFragmentById(R.id.loginFragment) as LoginFragment
                        loginFragment.loginError("invalid-password")
                    }
                    "user-disabled" -> {
                        val loginFragment =
                            supportFragmentManager.findFragmentById(R.id.loginFragment) as LoginFragment
                        loginFragment.loginError("user-disabled")
                    }
                }

                if (user.loggedIn(server)) {
                    navMenu.findItem(R.id.loginFragment).isVisible = false
                    navMenu.findItem(R.id.scanFragment).isVisible = true
                    navMenu.findItem(R.id.inventoryFragment).isVisible = true
                    navMenu.findItem(R.id.shelvesFragment).isVisible = true

                    val userView = findViewById<TextView>(R.id.userView)
                    userView.text = user.username

                    val navController =
                        supportFragmentManager.primaryNavigationFragment?.findNavController()

                    navController?.navigate(R.id.action_loginFragment_to_scanFragment)

                    val expandView = findViewById<TextView>(R.id.expandView)
                    expandView.text = getString(R.string.down_arrow)

                    navMenu.findItem(R.id.loginFragment).isVisible = false
                    navMenu.findItem(R.id.signoutLink).isVisible = false

                    navMenu.findItem(R.id.scanFragment).isVisible = true
                    navMenu.findItem(R.id.inventoryFragment).isVisible = true
                    navMenu.findItem(R.id.shelvesFragment).isVisible = true
                    navMenu.findItem(R.id.shoppingList).isVisible = true

                    val sharedPrefs = getPreferences(Context.MODE_PRIVATE)
                    with(sharedPrefs.edit()) {
                        putString(getString(R.string.server), server.url)
                        putString(getString(R.string.email), user.username)
                        putString(getString(R.string.token), user.token)
                        apply()
                    }
                }
            } else {
                val loginFragment =
                    supportFragmentManager.findFragmentById(R.id.loginFragment) as LoginFragment
                loginFragment.loginError("invalid-server")
            }
        }
    }

    override fun login(token: String, url: String) {
        TODO("Not yet implemented")
    }

    override fun register(email: String, password: String, url: String) {
        lifecycleScope.launch {
            if(server.connect(url)) {
                when(user.register(email, password, server)) {
                    "success" -> {
                        val navController = supportFragmentManager.primaryNavigationFragment?.findNavController()
                        navController?.navigate(R.id.action_registerFragment_to_loginFragment)
                    }
                    "user" -> {
                        val registerFragment = supportFragmentManager.findFragmentById(R.id.registerFragment) as RegisterFragment
                        registerFragment.registerError("invalid-user")
                    }
                }
            } else {
                val registerFragment = supportFragmentManager.findFragmentById(R.id.registerFragment) as RegisterFragment
                registerFragment.registerError("invalid-server")
            }
        }
    }
}