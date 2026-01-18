package com.example.lostandfoundsystem

import android.animation.Animator
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import com.airbnb.lottie.LottieAnimationView
import com.example.lostandfound.AdminHome
import com.example.lostandfound.R

class loading_screen : Fragment(R.layout.activity_loading_screen) {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.activity_loading_screen, container, false)

        // Handle edge-to-edge
        // Make sure it fills screen
        requireActivity().window.decorView.systemUiVisibility =
            View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LAYOUT_STABLE

        val waveAnim = view.findViewById<LottieAnimationView>(R.id.waveAnim)

        // Start from bottom
        waveAnim.progress = 0f
        waveAnim.speed = 5f
        waveAnim.playAnimation()

        waveAnim.addAnimatorListener(object : Animator.AnimatorListener {
            override fun onAnimationEnd(animation: Animator) {
                // Avoid double triggers
                waveAnim.removeAllAnimatorListeners()

                Handler(Looper.getMainLooper()).postDelayed({
                    // Reverse animation once
                    //waveAnim.speed = -5f
                    //waveAnim.playAnimation()

                    Handler(Looper.getMainLooper()).postDelayed({
                        val intent = Intent(requireContext(), AdminHome::class.java)
                        startActivity(intent)
                        requireActivity().finish() // optional: closes login activity

                    }, 600)
                }, 300)
            }

            override fun onAnimationStart(animation: Animator) {}
            override fun onAnimationCancel(animation: Animator) {}
            override fun onAnimationRepeat(animation: Animator) {}
        })

        return view
    }
}
