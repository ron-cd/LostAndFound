package activities.lostandfound.login

import android.animation.Animator
import android.animation.ObjectAnimator
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.airbnb.lottie.LottieAnimationView
import com.example.lostandfound.R

/**
 * Fragment responsible for the transition loading screen.
 * Uses Lottie animations for a wave effect and handles entry/exit transitions.
 */
class LoadingScreen : Fragment(R.layout.activity_loading_screen) {

    private lateinit var waveAnim: LottieAnimationView
    private var isAnimating = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.activity_loading_screen, container, false)

        // Setup Fullscreen UI flags
        @Suppress("DEPRECATION")
        requireActivity().window.decorView.systemUiVisibility =
            View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LAYOUT_STABLE

        waveAnim = view.findViewById(R.id.waveAnim)
        playCoverAnimation()

        return view
    }

    /**
     * Plays the initial forward animation to cover the screen.
     */
    private fun playCoverAnimation() {
        waveAnim.apply {
            progress = 0f
            speed = 8f
            playAnimation()
        }
    }

    /**
     * Reverses the wave animation and fades out the fragment view.
     * @param onFinished Callback executed once the fade-out and reversal are complete.
     */
    fun playReverseAnimation(onFinished: () -> Unit) {
        if (isAnimating || !this::waveAnim.isInitialized) return
        isAnimating = true

        waveAnim.removeAllAnimatorListeners()
        waveAnim.progress = 1f
        waveAnim.speed = -8f
        waveAnim.playAnimation()

        waveAnim.addAnimatorListener(object : Animator.AnimatorListener {
            override fun onAnimationEnd(animation: Animator) {
                waveAnim.removeAllAnimatorListeners()

                // Execute view fade-out after animation ends
                val fadeOut = ObjectAnimator.ofFloat(view, View.ALPHA, 1f, 0f)
                fadeOut.duration = 200
                fadeOut.start()

                // Finalize and trigger the callback
                Handler(Looper.getMainLooper()).postDelayed({
                    isAnimating = false
                    onFinished()
                }, fadeOut.duration)
            }

            override fun onAnimationStart(animation: Animator) {}
            override fun onAnimationCancel(animation: Animator) {}
            override fun onAnimationRepeat(animation: Animator) {}
        })
    }
}