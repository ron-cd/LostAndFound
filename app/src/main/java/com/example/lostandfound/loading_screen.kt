package com.example.lostandfound

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.animation.Animator
import android.os.Handler
import android.os.Looper
import android.view.View
import com.airbnb.lottie.LottieAnimationView
import com.airbnb.lottie.LottieDrawable

class loading_screen : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_loading_screen)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val waveAnim = findViewById<LottieAnimationView>(R.id.waveAnim)

// Start from bottom
        waveAnim.progress = 0f
        waveAnim.speed = 3f
        waveAnim.playAnimation()

        waveAnim.addAnimatorListener(object : Animator.AnimatorListener {

            override fun onAnimationEnd(animation: Animator) {

                // Avoid double triggers
                waveAnim.removeAllAnimatorListeners()

                Handler(Looper.getMainLooper()).postDelayed({


                    // Reverse animation once
                    waveAnim.speed = -3f
                    waveAnim.playAnimation()

                }, 250)
            }

            override fun onAnimationStart(animation: Animator) {}
            override fun onAnimationCancel(animation: Animator) {}
            override fun onAnimationRepeat(animation: Animator) {}
        })


    }
}