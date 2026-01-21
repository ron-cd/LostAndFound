package com.example.lostandfoundsystem

import android.content.Intent
import android.os.Bundle
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.view.*
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.lostandfound.Login
import com.example.lostandfound.R
import com.example.lostandfound.SuccessScreen
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

class Registration : Fragment() {

    private var isPasswordVisible = false

    private fun setupPasswordToggle(editText: EditText, toggleIcon: ImageView) {
        toggleIcon.setOnClickListener {
            if (isPasswordVisible) {
                // Hide password
                editText.transformationMethod = PasswordTransformationMethod.getInstance()
                toggleIcon.setImageResource(R.drawable.hide)
            } else {
                // Show password
                editText.transformationMethod = HideReturnsTransformationMethod.getInstance()
                toggleIcon.setImageResource(R.drawable.view)
            }
            isPasswordVisible = !isPasswordVisible
            // Move cursor to end after toggle
            editText.setSelection(editText.text.length)
        }
    }

    fun isValidNUEmail(email: String): Boolean {
        return email.endsWith("@students.nu-clark.edu.ph") ||
                email.endsWith("@nu-clark.edu.ph")
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.activity_registration, container, false)
        val activity = requireActivity() as AppCompatActivity

        // ✅ Make fragment full-screen
        activity.window.apply {
            decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
        }

        // ✅ Find your back ImageView and make it go back to login
        val backButton = view.findViewById<ImageView>(R.id.IV_Back)
        val signup = view.findViewById<Button>(R.id.BTN_Signup)
        val emailET = view.findViewById<EditText>(R.id.ET_Email)
        val passwordET = view.findViewById<EditText>(R.id.ET_Password)
        val usernameET = view.findViewById<EditText>(R.id.ET_Username)
        val confirmPasswordEditText = view.findViewById<EditText>(R.id.ET_ConfirmPass)
        val toggleIconConfirm = view.findViewById<ImageView>(R.id.IV_Confirm)
        val toggleIconPass = view.findViewById<ImageView>(R.id.IV_ShowPass)
        val auth = FirebaseAuth.getInstance()
        val firestore = FirebaseFirestore.getInstance()

        backButton.setOnClickListener {
            val intent = Intent(requireContext(), Login::class.java)
            startActivity(intent)
            requireActivity().finish()
        }

        signup.setOnClickListener {
            val email = emailET.text.toString().trim()
            val password = passwordET.text.toString().trim()
            val username = usernameET.text.toString().trim()

            // Validate fields
            if (email.isEmpty() || password.isEmpty() || username.isEmpty()) {
                Toast.makeText(requireContext(), "Please fill in all fields.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (!isValidNUEmail(email)) {
                Toast.makeText(requireContext(), "Please use a valid NU student email.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Create user
            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val uid = auth.currentUser?.uid

                        val userData = hashMapOf(
                            "username" to username,
                            "email" to email,
                            "role" to "student",
                            "password" to password,
                            "approved" to false,
                            "createdAt" to FieldValue.serverTimestamp()
                        )

                        if (uid != null) {
                            firestore.collection("users")
                                .document(uid)
                                .set(userData)
                                .addOnSuccessListener {
                                    val intent = Intent(requireContext(), SuccessScreen::class.java)
                                    startActivity(intent)
                                    requireActivity().finish()
                                }
                                .addOnFailureListener { e ->
                                    Toast.makeText(requireContext(), "Error saving user: ${e.message}", Toast.LENGTH_SHORT).show()
                                }
                        }

                    } else {
                        Toast.makeText(requireContext(), "Signup failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                    }
                }
        }

        setupPasswordToggle(passwordET, toggleIconPass)
        setupPasswordToggle(confirmPasswordEditText, toggleIconConfirm )

        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()

        val activity = requireActivity() as AppCompatActivity
        // ✅ Clear full-screen flags when leaving
        activity.window.clearFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
    }
}
