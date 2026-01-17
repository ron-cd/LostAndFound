package com.example.lostandfound

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity


class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val intent = Intent(this, loading_screen::class.java)
        startActivity(intent)

        finish()
    }
}
