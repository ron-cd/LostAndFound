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
            }, 1600) // delay in milliseconds (adjust as needed)

            Handler(Looper.getMainLooper()).postDelayed({
                loadingFragment.playReverseAnimation {
                    supportFragmentManager.beginTransaction()
                        .remove(loadingFragment)
                        .commitNowAllowingStateLoss()
                }
            }, 1500) // make sure this delay matches your animation timing
        }


        binding.BTNLogin.setOnClickListener {
            val email = emailet.text.toString().trim()
            val username = usernameet.text.toString().trim()
            val password = passwordet.text.toString().trim()

            if (email.isNotEmpty() && username.isNotEmpty() && password.isNotEmpty()) {
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