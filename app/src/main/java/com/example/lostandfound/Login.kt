package com.example.lostandfound


import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import com.example.lostandfound.databinding.ActivityAdminhomeBinding
import com.example.lostandfound.databinding.ActivityLoginBinding
import com.example.lostandfoundsystem.Registration
import com.example.lostandfoundsystem.loading_screen
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.time.delay
import kotlin.math.log

class Login : AppCompatActivity() {



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

        val loginbutton = findViewById<Button>(R.id.BTN_Login)
        val emailet = findViewById<TextView>(R.id.ET_Email)
        val usernameet = findViewById<TextView>(R.id.ET_Username)
        val passwordet = findViewById<TextView>(R.id.ET_Password)
        val confirmpass = findViewById<TextView>(R.id.ET_ConfirmPass)

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
            val firestore = FirebaseFirestore.getInstance()
            val email = emailet.text.toString().trim()
            val username = usernameet.text.toString().trim()
            val password = passwordet.text.toString().trim()
            val confirmpass = confirmpass.text.toString().trim()
            val isAdmin =
                username == "Admin" &&
                        email == "NUSdao@admin.nu-clark.edu.ph" &&
                        password == "NUCRK202"

            if (email.isNotEmpty() && username.isNotEmpty() && password.isNotEmpty() && password == confirmpass ) {
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
                if(!isAdmin){

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
                }
            } else {
                Toast.makeText(this, "Please fill in all fields.", Toast.LENGTH_SHORT).show()
            }
        }



    }
    private fun replaceFragment(fragment: Fragment) {
        val fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.FM_Container, fragment)
        fragmentTransaction.commit()
    }

}