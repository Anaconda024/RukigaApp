package com.example.rukigaapp

import android.os.Bundle
import android.view.Menu
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import androidx.navigation.ui.NavigationUI
import com.example.rukigaapp.databinding.ActivityMainBinding
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()

        setSupportActionBar(binding.appBarMain.toolbar)
        // Hide the title to show only the hamburger icon
        supportActionBar?.setDisplayShowTitleEnabled(false)

        // Alternative way to get NavController - uses FragmentManager which is more reliable
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment_content_main) as? NavHostFragment

        if (navHostFragment == null) {
            Toast.makeText(
                this,
                "ERROR: NavHostFragment not found! Check content_main.xml",
                Toast.LENGTH_LONG
            ).show()
            finish()
            return
        }

        val navController = navHostFragment.navController

        // Set dynamic start destination based on authentication status
        val navGraph = navController.navInflater.inflate(R.navigation.mobile_navigation)
        navGraph.setStartDestination(
            if (auth.currentUser != null) R.id.nav_home else R.id.loginFragment
        )
        navController.graph = navGraph

        // Configure app bar with navigation
        appBarConfiguration = AppBarConfiguration(
            setOf(R.id.nav_home, R.id.nav_dictionary, R.id.nav_quiz, R.id.loginFragment),
            binding.drawerLayout
        )

        setupActionBarWithNavController(navController, appBarConfiguration)
        binding.navView.setupWithNavController(navController)

        // Handle navigation menu item clicks including logout
        binding.navView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_logout -> {
                    handleLogout(navController)
                    binding.drawerLayout.closeDrawers()
                    true
                }
                else -> {
                    val handled = NavigationUI.onNavDestinationSelected(menuItem, navController)
                    if (handled) {
                        binding.drawerLayout.closeDrawers()
                    }
                    handled
                }
            }
        }
    }

    private fun handleLogout(navController: androidx.navigation.NavController) {
        // Sign out from Firebase
        auth.signOut()

        // Navigate to login screen and clear back stack
        try {
            navController.navigate(R.id.loginFragment) {
                popUpTo(0) { inclusive = true }
            }
            Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(this, "Logged out", Toast.LENGTH_SHORT).show()
            e.printStackTrace()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onSupportNavigateUp(): Boolean {
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment_content_main) as? NavHostFragment

        return if (navHostFragment != null) {
            navHostFragment.navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
        } else {
            super.onSupportNavigateUp()
        }
    }
}