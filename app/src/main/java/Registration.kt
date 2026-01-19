package com.example.lostandfoundsystem

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.lostandfound.R

class Registration : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.activity_registration, container, false)

        // Hide action bar for full-screen effect
        (requireActivity() as AppCompatActivity).supportActionBar?.hide()

        // Make fragment truly full-screen
        requireActivity().window.apply {
            // Allow layout to extend behind system bars
            decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            // Remove limits so fragment covers everything
            addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
        }

        // Setup your views here (buttons, inputs, etc.)
        // Example:
        // val btnRegister = view.findViewById<Button>(R.id.btnRegister)
        // btnRegister.setOnClickListener { ... }

        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Restore action bar when leaving
        (requireActivity() as AppCompatActivity).supportActionBar?.show()
        // Optionally clear full-screen flags if needed
        requireActivity().window.clearFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
    }
}
