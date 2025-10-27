package com.example.loginandregistration.views

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.example.loginandregistration.R

/**
 * Custom view that displays connection status banner at the top of the screen. Shows different
 * states: offline, connecting, and hides when online.
 */
class ConnectionStatusView
@JvmOverloads
constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) :
        LinearLayout(context, attrs, defStyleAttr) {

    private val statusIcon: ImageView
    private val statusText: TextView
    private val banner: LinearLayout

    private var isAnimating = false

    init {
        LayoutInflater.from(context).inflate(R.layout.view_connection_status, this, true)

        banner = findViewById(R.id.connectionStatusBanner)
        statusIcon = findViewById(R.id.connectionStatusIcon)
        statusText = findViewById(R.id.connectionStatusText)
    }

    /** Show "No internet connection" banner */
    fun showOffline() {
        if (isAnimating) return

        statusIcon.setImageResource(android.R.drawable.stat_notify_error)
        statusText.text = context.getString(R.string.no_internet_connection)
        banner.setBackgroundColor(ContextCompat.getColor(context, R.color.colorError))

        slideDown()
    }

    /** Show "Connecting..." banner */
    fun showConnecting() {
        if (isAnimating) return

        statusIcon.setImageResource(android.R.drawable.stat_notify_sync)
        statusText.text = context.getString(R.string.connecting)
        banner.setBackgroundColor(ContextCompat.getColor(context, R.color.colorWarning))

        if (banner.visibility != View.VISIBLE) {
            slideDown()
        }
    }

    /** Hide banner when online */
    fun showOnline() {
        if (isAnimating) return

        if (banner.visibility == View.VISIBLE) {
            slideUp()
        }
    }

    /** Animate banner sliding down from top */
    private fun slideDown() {
        if (banner.visibility == View.VISIBLE) return

        isAnimating = true
        banner.visibility = View.VISIBLE
        banner.translationY = -banner.height.toFloat()

        banner.animate()
                .translationY(0f)
                .setDuration(300)
                .setListener(
                        object : AnimatorListenerAdapter() {
                            override fun onAnimationEnd(animation: Animator) {
                                isAnimating = false
                            }
                        }
                )
                .start()
    }

    /** Animate banner sliding up and hiding */
    private fun slideUp() {
        if (banner.visibility != View.VISIBLE) return

        isAnimating = true

        banner.animate()
                .translationY(-banner.height.toFloat())
                .setDuration(300)
                .setListener(
                        object : AnimatorListenerAdapter() {
                            override fun onAnimationEnd(animation: Animator) {
                                banner.visibility = View.GONE
                                banner.translationY = 0f
                                isAnimating = false
                            }
                        }
                )
                .start()
    }

    /** Force hide without animation (useful for initialization) */
    fun hideImmediate() {
        banner.visibility = View.GONE
        banner.translationY = 0f
        isAnimating = false
    }
}
