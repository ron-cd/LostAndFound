package activities.lostandfound.login

import android.content.Intent
import android.os.Bundle
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.lostandfound.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

/**
 * Fragment responsible for user account creation.
 * Includes custom back navigation to return to the Login activity.
 */
class Registration : Fragment() {

    private var isPasswordVisible = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.activity_registration, container, false)

        setupFullscreenMode()
        setupBackNavigation() // Handles Android system back button/gesture
        initializeRegistrationLogic(view)

        return view
    }

    /**
     * Intercepts the system back button/gesture to navigate back to Login.
     */
    private fun setupBackNavigation() {
        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    navigateToLogin()
                }
            }
        )
    }

    /**
     * Configures the activity window for a seamless fullscreen experience.
     */
    private fun setupFullscreenMode() {
        val activity = requireActivity() as AppCompatActivity
        @Suppress("DEPRECATION")
        activity.window.apply {
            decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
        }
    }

    /**
     * Centralized initialization for view references and click listeners.
     */
    private fun initializeRegistrationLogic(view: View) {
        val auth = FirebaseAuth.getInstance()
        val firestore = FirebaseFirestore.getInstance()

        // View References
        val backButton = view.findViewById<ImageView>(R.id.IV_Back)
        val signupBtn = view.findViewById<Button>(R.id.BTN_Signup)
        val emailEt = view.findViewById<EditText>(R.id.ET_Email)
        val passwordEt = view.findViewById<EditText>(R.id.ET_Password)
        val usernameEt = view.findViewById<EditText>(R.id.ET_Username)
        val confirmPassEt = view.findViewById<EditText>(R.id.ET_ConfirmPass)
        val toggleIconPass = view.findViewById<ImageView>(R.id.IV_ShowPass)
        val toggleIconConfirm = view.findViewById<ImageView>(R.id.IV_Confirm)

        // Password Toggles
        setupPasswordToggle(passwordEt, toggleIconPass)
        setupPasswordToggle(confirmPassEt, toggleIconConfirm)

        // UI Back Button
        backButton.setOnClickListener {
            navigateToLogin()
        }

        // Registration Action
        signupBtn.setOnClickListener {
            handleSignup(emailEt, passwordEt, usernameEt, auth, firestore)
        }
    }

    /**
     * Common logic to return to the Login Activity.
     */
    private fun navigateToLogin() {
        val intent = Intent(requireContext(), Login::class.java)
        startActivity(intent)
        requireActivity().finish()
    }

    /**
     * Toggles between hidden and visible password text.
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

    private fun isValidNUEmail(email: String): Boolean {
        return email.endsWith("@students.nu-clark.edu.ph") ||
                email.endsWith("@nu-clark.edu.ph")
    }

    private fun handleSignup(
        emailEt: EditText,
        passwordEt: EditText,
        usernameEt: EditText,
        auth: FirebaseAuth,
        firestore: FirebaseFirestore
    ) {
        val email = emailEt.text.toString().trim()
        val password = passwordEt.text.toString().trim()
        val username = usernameEt.text.toString().trim()

        if (email.isEmpty() || password.isEmpty() || username.isEmpty()) {
            Toast.makeText(requireContext(), "Please fill in all fields.", Toast.LENGTH_SHORT).show()
            return
        }

        if (!isValidNUEmail(email)) {
            Toast.makeText(requireContext(), "Please use a valid NU student email.", Toast.LENGTH_SHORT).show()
            return
        }

        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val uid = auth.currentUser?.uid ?: return@addOnCompleteListener

                    val userData = hashMapOf(
                        "username" to username,
                        "email" to email,
                        "role" to "student",
                        "approved" to false,
                        "createdAt" to FieldValue.serverTimestamp()
                    )

                    firestore.collection("users")
                        .document(uid)
                        .set(userData)
                        .addOnSuccessListener {
                            startActivity(Intent(requireContext(), SuccessScreen::class.java))
                            requireActivity().finish()
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(requireContext(), "Error saving user: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                } else {
                    Toast.makeText(requireContext(), "Signup failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        val activity = requireActivity() as AppCompatActivity
        activity.window.clearFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
    }
}