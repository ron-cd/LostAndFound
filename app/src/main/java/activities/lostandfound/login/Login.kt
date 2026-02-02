package activities.lostandfound.login

import activities.lostandfound.adminview.AdminHome
import activities.lostandfound.studentview.MainHome
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.lostandfound.R
import com.example.lostandfound.databinding.ActivityLoginBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

/**
 * Main entry point for the application.
 * Handles Admin authentication, Firebase Student login, and Fragment transitions.
 */
class Login : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private var isPasswordVisible = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)

        setupWindowInsets()
        initializeLoginLogic()
    }

    /**
     * Handles system bar padding for edge-to-edge display.
     */
    private fun setupWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, 10, systemBars.right, systemBars.bottom)
            insets
        }
    }

    /**
     * Entry point for setting up UI listeners and auth logic.
     */
    private fun initializeLoginLogic() {
        // UI References via ViewBinding and findViewById
        val emailEt = findViewById<TextView>(R.id.ET_Email)
        val usernameEt = findViewById<EditText>(R.id.ET_Username)
        val passwordEt = findViewById<EditText>(R.id.ET_Password)
        val showIcon = findViewById<ImageView>(R.id.IV_ShowPassword)

        setupPasswordToggle(passwordEt, showIcon)

        // Navigation to Registration Fragment
        binding.TVSignup.setOnClickListener {
            handleSignupTransition()
        }

        // Authentication Action
        binding.BTNLogin.setOnClickListener {
            handleLogin(emailEt, usernameEt, passwordEt)
        }
    }

    /**
     * Logic for toggling password visibility in the EditText.
     */
    private fun setupPasswordToggle(editText: EditText, toggleIcon: ImageView) {
        toggleIcon.setOnClickListener {
            if (isPasswordVisible) {
                editText.transformationMethod = PasswordTransformationMethod.getInstance()
                toggleIcon.setImageResource(R.drawable.hide)
            } else {
                editText.transformationMethod = HideReturnsTransformationMethod.getInstance()
                toggleIcon.setImageResource(R.drawable.view)
            }
            isPasswordVisible = !isPasswordVisible
            editText.setSelection(editText.text.length)
        }
    }

    /**
     * Manages the loading screen and registration fragment transition.
     */
    private fun handleSignupTransition() {
        val loadingFragment = LoadingScreen()
        val registrationFragment = Registration()

        supportFragmentManager.beginTransaction()
            .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
            .add(R.id.FM_Container, loadingFragment, "loadingFragment")
            .commit()

        // Delay adding registration fragment
        Handler(Looper.getMainLooper()).postDelayed({
            supportFragmentManager.beginTransaction()
                .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
                .add(R.id.FM_Container, registrationFragment)
                .commit()
        }, 1100)

        // Play reverse animation and remove loading fragment
        Handler(Looper.getMainLooper()).postDelayed({
            loadingFragment.playReverseAnimation {
                supportFragmentManager.beginTransaction()
                    .remove(loadingFragment)
                    .commitNowAllowingStateLoss()
            }
        }, 1000)
    }

    /**
     * Handles the login logic for both Admin and Students.
     */
    private fun handleLogin(emailEt: TextView, usernameEt: EditText, passwordEt: EditText) {
        val auth = FirebaseAuth.getInstance()

        FirebaseAuth.getInstance().signOut()

        val email = emailEt.text.toString().trim()
        val password = passwordEt.text.toString().trim()

        if (email.isNotEmpty() && password.isNotEmpty()) {
            // Always sign in to Firebase so you have a valid 'request.auth'
            auth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener {
                    // Now check if this authenticated user is the Admin
                    if (email == "NUSdao@admin.nu-clark.edu.ph") {
                        navigateToHome(AdminHome::class.java)
                    } else {
                        checkUserApproval()
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Incorrect email or password.", Toast.LENGTH_SHORT).show()
                }
        } else {
            Toast.makeText(this, "Please fill in all fields.", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Checks if the student account has been approved by the Admin in Firestore.
     */
    private fun checkUserApproval() {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return

        FirebaseFirestore.getInstance()
            .collection("users")
            .document(uid)
            .get()
            .addOnSuccessListener { doc ->
                val approved = doc.getBoolean("approved") ?: false

                if (!approved) {
                    Toast.makeText(this, "Please wait for admin approval.", Toast.LENGTH_SHORT).show()
                    FirebaseAuth.getInstance().signOut()
                } else {
                    navigateToHome(MainHome::class.java)
                }
            }
    }

    /**
     * Shared method to show loading screen and launch home activities.
     */
    private fun navigateToHome(targetActivity: Class<*>) {
        Toast.makeText(this, "Logged in Successfully!", Toast.LENGTH_SHORT).show()

        val loadingFragment = LoadingScreen()
        supportFragmentManager.beginTransaction()
            .replace(R.id.FM_Container, loadingFragment, "loadingFragment")
            .commit()

        Handler(Looper.getMainLooper()).postDelayed({
            startActivity(Intent(this, targetActivity))
            finish()
        }, 1600)
    }

}