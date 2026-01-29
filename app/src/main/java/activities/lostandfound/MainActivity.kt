package activities.lostandfound

import activities.lostandfound.login.Login
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate

/**
 * Entry point activity that handles initial app configuration
 * and routes the user to the Login screen.
 */
class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Force Light Mode for the entire application
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)

        // Route to Login Activity
        navigateToLogin()
    }

    /**
     * Transitions to the Login activity and finishes the current activity
     * to prevent the user from returning to this empty screen.
     */
    private fun navigateToLogin() {
        val intent = Intent(this, Login::class.java)
        startActivity(intent)
        finish()
    }
}