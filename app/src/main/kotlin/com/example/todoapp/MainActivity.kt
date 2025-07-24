package com.example.todoapp

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupActionBarWithNavController
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    // Suppress lint warning about view inflation timing
    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Set the layout for this activity
        setContentView(R.layout.activity_main)

        // Find Toolbar view and set it as the ActionBar for this activity
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)  // Important: Toolbar acts as ActionBar

        // Check if app has notification permission (required from Android 13+)
        checkNotificationPermission()

        // Setup Navigation Component with toolbar
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController

        // Connect toolbar's back button with NavController's navigation stack
        setupActionBarWithNavController(navController)
    }

    // Request POST_NOTIFICATIONS permission on Android 13+ if not already granted
    private fun checkNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val permission = Manifest.permission.POST_NOTIFICATIONS
            // Check if permission already granted
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                // Request the permission from user
                ActivityCompat.requestPermissions(this, arrayOf(permission), 1001)
            }
        }
    }

    // Handle the "Up" button behavior in the toolbar for navigation
    override fun onSupportNavigateUp(): Boolean {
        val navController = (supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment).navController
        // Navigate up in the navigation stack or fallback to default behavior
        return navController.navigateUp() || super.onSupportNavigateUp()
    }
}
