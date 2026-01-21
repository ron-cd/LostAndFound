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
import androidx.fragment.app.Fragment
import activities.lostandfound.login.loading_screen
import com.example.lostandfound.R
import com.example.lostandfound.databinding.ActivityLoginBinding
import activities.lostandfound.login.Registration
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class Login : AppCompatActivity() {

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

    private lateinit var binding: ActivityLoginBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        enableEdgeToEdge()
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, 10, systemBars.right, systemBars.bottom)
            insets
        }

        val emailet = findViewById<TextView>(R.id.ET_Email)
        val usernameet = findViewById<EditText>(R.id.ET_Username)
        val passwordet = findViewById<EditText>(R.id.ET_Password)
        val showicon = findViewById<ImageView>(R.id.IV_ShowPassword)


        binding.TVSignup.setOnClickListener {
            val loadingFragment = loading_screen()
            val registrationFragment = Registration()

            supportFragmentManager.beginTransaction()
                .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
                .add(R.id.FM_Container, loadingFragment, "loadingFragment")
                .commit()

            // 2️⃣ Delay adding the registration fragment
            Handler(Looper.getMainLooper()).postDelayed({
                supportFragmentManager.beginTransaction()
                    .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
                    .add(R.id.FM_Container, registrationFragment)
                    .commit()
            }, 1100) // delay in milliseconds (adjust as needed)

            Handler(Looper.getMainLooper()).postDelayed({
                loadingFragment.playReverseAnimation {
                    supportFragmentManager.beginTransaction()
                        .remove(loadingFragment)
                        .commitNowAllowingStateLoss()
                }
            }, 1000) // make sure this delay matches your animation timing
        }


        binding.BTNLogin.setOnClickListener {
            val auth = FirebaseAuth.getInstance()
            val email = emailet.text.toString().trim()
            val username = usernameet.text.toString().trim()
            val password = passwordet.text.toString().trim()
            val isAdmin =
                username == "Admin" &&
                        email == "NUSdao@admin.nu-clark.edu.ph" &&
                        password == "NUCRK202"

            if (email.isNotEmpty() && username.isNotEmpty() && password.isNotEmpty() ) {
                if(isAdmin) {

                    val loadingFragment = loading_screen()
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.FM_Container, loadingFragment, "loadingFragment")
                        .commit()

                    // Delay slightly so the animation can play before launching AdminHome
                    Handler(Looper.getMainLooper()).postDelayed({
                        val intent = Intent(this, AdminHome::class.java)
                        startActivity(intent)
                        finish()
                    }, 1600) // Adjust to match your Lottie animation length
                }
                else{
                    auth.signInWithEmailAndPassword(email, password)
                        .addOnSuccessListener {
                            val uid = FirebaseAuth.getInstance().currentUser!!.uid

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

                                        Toast.makeText(this, "Logged in Successfully!", Toast.LENGTH_SHORT).show()

                                        val loadingFragment = loading_screen()
                                        supportFragmentManager.beginTransaction()
                                            .replace(R.id.FM_Container, loadingFragment, "loadingFragment")
                                            .commit()


                                        Handler(Looper.getMainLooper()).postDelayed({
                                            val intent = Intent(this, MainHome::class.java)
                                            startActivity(intent)
                                            finish()
                                        }, 1600) // Adjust to match your Lottie animation length
                                    }
                                }
                        }
                        .addOnFailureListener {
                            Toast.makeText(this, "Incorrect email or password.", Toast.LENGTH_SHORT).show()
                        }
                }
            } else {
                Toast.makeText(this, "Please fill in all fields.", Toast.LENGTH_SHORT).show()
            }
        }

        setupPasswordToggle(passwordet, showicon)

    }
    private fun replaceFragment(fragment: Fragment) {
        val fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.FM_Container, fragment)
        fragmentTransaction.commit()
    }

}