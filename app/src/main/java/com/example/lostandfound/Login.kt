package com.example.lostandfound

import android.content.Intent
import android.os.Bundle
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
import com.example.lostandfoundsystem.loading_screen
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
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val loginbutton = findViewById<Button>(R.id.BTN_Login)
        val emailet = findViewById<TextView>(R.id.ET_Email)
        val usernameet = findViewById<TextView>(R.id.ET_Username)
        val passwordet = findViewById<TextView>(R.id.ET_Password)

        binding.BTNLogin.setOnClickListener {
            val email = emailet.text.toString().trim()
            val username = usernameet.text.toString().trim()
            val password = passwordet.text.toString().trim()

            if (email.isNotEmpty() && username.isNotEmpty() && password.isNotEmpty()) {
                replaceFragment(loading_screen())
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