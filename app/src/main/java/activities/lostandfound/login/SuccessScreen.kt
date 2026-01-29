package activities.lostandfound.login

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.lostandfound.R

/**
 * Activity displayed upon successful registration.
 * Automatically redirects the user back to the Login screen after a short delay.
 */
class SuccessScreen : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_success_screen)

        setupWindowInsets()
        startRedirectTimer()
    }

    /**
     * Handles system bar padding for edge-to-edge display.
     */
    private fun setupWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    /**
     * Delays for 2 seconds then navigates back to the Login Activity.
     * finish() is called to ensure the user cannot navigate back to this screen.
     */
    private fun startRedirectTimer() {
        Handler(Looper.getMainLooper()).postDelayed({
            val intent = Intent(this, Login::class.java)
            startActivity(intent)

            // Close SuccessScreen so user can’t go back to it
            finish()
        }, 2000)
    }
}