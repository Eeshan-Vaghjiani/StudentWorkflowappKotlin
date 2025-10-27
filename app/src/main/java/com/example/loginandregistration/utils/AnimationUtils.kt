package com.example.loginandregistration.utils

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.DecelerateInterpolator
import android.view.animation.OvershootInterpolator
import androidx.recyclerview.widget.RecyclerView

/**
 * Utility class for common animations throughout the app. Provides fade, slide, scale, and other
 * animation effects.
 */
object AnimationUtils {

    // Default animation durations
    private const val DURATION_SHORT = 200L
    private const val DURATION_MEDIUM = 300L
    private const val DURATION_LONG = 400L

    /**
     * Fade in animation - makes a view visible with fade effect
     * @param view The view to animate
     * @param duration Animation duration in milliseconds
     * @param onEnd Optional callback when animation ends
     */
    fun fadeIn(view: View, duration: Long = DURATION_MEDIUM, onEnd: (() -> Unit)? = null) {
        view.alpha = 0f
        view.visibility = View.VISIBLE

        view.animate()
                .alpha(1f)
                .setDuration(duration)
                .setInterpolator(DecelerateInterpolator())
                .setListener(
                        object : AnimatorListenerAdapter() {
                            override fun onAnimationEnd(animation: Animator) {
                                onEnd?.invoke()
                            }
                        }
                )
                .start()
    }

    /**
     * Fade out animation - hides a view with fade effect
     * @param view The view to animate
     * @param duration Animation duration in milliseconds
     * @param onEnd Optional callback when animation ends
     */
    fun fadeOut(view: View, duration: Long = DURATION_MEDIUM, onEnd: (() -> Unit)? = null) {
        view.animate()
                .alpha(0f)
                .setDuration(duration)
                .setInterpolator(DecelerateInterpolator())
                .setListener(
                        object : AnimatorListenerAdapter() {
                            override fun onAnimationEnd(animation: Animator) {
                                view.visibility = View.GONE
                                onEnd?.invoke()
                            }
                        }
                )
                .start()
    }

    /**
     * Slide up animation - slides a view up from bottom
     * @param view The view to animate
     * @param duration Animation duration in milliseconds
     * @param onEnd Optional callback when animation ends
     */
    fun slideUp(view: View, duration: Long = DURATION_MEDIUM, onEnd: (() -> Unit)? = null) {
        view.visibility = View.VISIBLE
        view.translationY = view.height.toFloat()
        view.alpha = 0f

        view.animate()
                .translationY(0f)
                .alpha(1f)
                .setDuration(duration)
                .setInterpolator(DecelerateInterpolator())
                .setListener(
                        object : AnimatorListenerAdapter() {
                            override fun onAnimationEnd(animation: Animator) {
                                onEnd?.invoke()
                            }
                        }
                )
                .start()
    }

    /**
     * Slide down animation - slides a view down and hides it
     * @param view The view to animate
     * @param duration Animation duration in milliseconds
     * @param onEnd Optional callback when animation ends
     */
    fun slideDown(view: View, duration: Long = DURATION_MEDIUM, onEnd: (() -> Unit)? = null) {
        view.animate()
                .translationY(view.height.toFloat())
                .alpha(0f)
                .setDuration(duration)
                .setInterpolator(DecelerateInterpolator())
                .setListener(
                        object : AnimatorListenerAdapter() {
                            override fun onAnimationEnd(animation: Animator) {
                                view.visibility = View.GONE
                                view.translationY = 0f
                                onEnd?.invoke()
                            }
                        }
                )
                .start()
    }

    /**
     * Scale in animation - scales a view from small to normal size
     * @param view The view to animate
     * @param duration Animation duration in milliseconds
     * @param onEnd Optional callback when animation ends
     */
    fun scaleIn(view: View, duration: Long = DURATION_SHORT, onEnd: (() -> Unit)? = null) {
        view.scaleX = 0.8f
        view.scaleY = 0.8f
        view.alpha = 0f
        view.visibility = View.VISIBLE

        view.animate()
                .scaleX(1f)
                .scaleY(1f)
                .alpha(1f)
                .setDuration(duration)
                .setInterpolator(OvershootInterpolator())
                .setListener(
                        object : AnimatorListenerAdapter() {
                            override fun onAnimationEnd(animation: Animator) {
                                onEnd?.invoke()
                            }
                        }
                )
                .start()
    }

    /**
     * Scale out animation - scales a view from normal to small size and hides it
     * @param view The view to animate
     * @param duration Animation duration in milliseconds
     * @param onEnd Optional callback when animation ends
     */
    fun scaleOut(view: View, duration: Long = DURATION_SHORT, onEnd: (() -> Unit)? = null) {
        view.animate()
                .scaleX(0.8f)
                .scaleY(0.8f)
                .alpha(0f)
                .setDuration(duration)
                .setInterpolator(AccelerateDecelerateInterpolator())
                .setListener(
                        object : AnimatorListenerAdapter() {
                            override fun onAnimationEnd(animation: Animator) {
                                view.visibility = View.GONE
                                view.scaleX = 1f
                                view.scaleY = 1f
                                onEnd?.invoke()
                            }
                        }
                )
                .start()
    }

    /**
     * Button press animation - scales down and back up for tactile feedback
     * @param view The view to animate (typically a button)
     */
    fun buttonPress(view: View) {
        view.animate()
                .scaleX(0.95f)
                .scaleY(0.95f)
                .setDuration(100)
                .setInterpolator(DecelerateInterpolator())
                .withEndAction {
                    view.animate()
                            .scaleX(1f)
                            .scaleY(1f)
                            .setDuration(100)
                            .setInterpolator(AccelerateDecelerateInterpolator())
                            .start()
                }
                .start()
    }

    /**
     * Smooth scroll animation for RecyclerView
     * @param recyclerView The RecyclerView to scroll
     * @param position The position to scroll to
     * @param smooth Whether to use smooth scrolling
     */
    fun smoothScrollToPosition(recyclerView: RecyclerView, position: Int, smooth: Boolean = true) {
        if (smooth) {
            recyclerView.smoothScrollToPosition(position)
        } else {
            recyclerView.scrollToPosition(position)
        }
    }

    /**
     * Smooth scroll to bottom of RecyclerView
     * @param recyclerView The RecyclerView to scroll
     */
    fun smoothScrollToBottom(recyclerView: RecyclerView) {
        val adapter = recyclerView.adapter
        if (adapter != null && adapter.itemCount > 0) {
            recyclerView.smoothScrollToPosition(adapter.itemCount - 1)
        }
    }

    /**
     * Fade transition between two views
     * @param viewOut The view to fade out
     * @param viewIn The view to fade in
     * @param duration Animation duration in milliseconds
     */
    fun crossFade(viewOut: View, viewIn: View, duration: Long = DURATION_MEDIUM) {
        viewIn.alpha = 0f
        viewIn.visibility = View.VISIBLE

        viewIn.animate().alpha(1f).setDuration(duration).setListener(null)

        viewOut.animate()
                .alpha(0f)
                .setDuration(duration)
                .setListener(
                        object : AnimatorListenerAdapter() {
                            override fun onAnimationEnd(animation: Animator) {
                                viewOut.visibility = View.GONE
                            }
                        }
                )
    }

    /**
     * Shake animation for error feedback
     * @param view The view to shake
     */
    fun shake(view: View) {
        val animator =
                ObjectAnimator.ofFloat(
                        view,
                        "translationX",
                        0f,
                        25f,
                        -25f,
                        25f,
                        -25f,
                        15f,
                        -15f,
                        6f,
                        -6f,
                        0f
                )
        animator.duration = 500
        animator.start()
    }

    /**
     * Pulse animation - scales view slightly larger and back
     * @param view The view to pulse
     */
    fun pulse(view: View) {
        view.animate()
                .scaleX(1.1f)
                .scaleY(1.1f)
                .setDuration(200)
                .setInterpolator(AccelerateDecelerateInterpolator())
                .withEndAction {
                    view.animate()
                            .scaleX(1f)
                            .scaleY(1f)
                            .setDuration(200)
                            .setInterpolator(AccelerateDecelerateInterpolator())
                            .start()
                }
                .start()
    }

    /**
     * Rotate animation
     * @param view The view to rotate
     * @param degrees The degrees to rotate (360 for full rotation)
     * @param duration Animation duration in milliseconds
     */
    fun rotate(view: View, degrees: Float = 360f, duration: Long = DURATION_MEDIUM) {
        view.animate()
                .rotation(degrees)
                .setDuration(duration)
                .setInterpolator(AccelerateDecelerateInterpolator())
                .start()
    }

    /**
     * Slide in from right animation
     * @param view The view to animate
     * @param duration Animation duration in milliseconds
     */
    fun slideInFromRight(
            view: View,
            duration: Long = DURATION_MEDIUM,
            onEnd: (() -> Unit)? = null
    ) {
        view.visibility = View.VISIBLE
        view.translationX = view.width.toFloat()
        view.alpha = 0f

        view.animate()
                .translationX(0f)
                .alpha(1f)
                .setDuration(duration)
                .setInterpolator(DecelerateInterpolator())
                .setListener(
                        object : AnimatorListenerAdapter() {
                            override fun onAnimationEnd(animation: Animator) {
                                onEnd?.invoke()
                            }
                        }
                )
                .start()
    }

    /**
     * Slide in from left animation
     * @param view The view to animate
     * @param duration Animation duration in milliseconds
     */
    fun slideInFromLeft(view: View, duration: Long = DURATION_MEDIUM, onEnd: (() -> Unit)? = null) {
        view.visibility = View.VISIBLE
        view.translationX = -view.width.toFloat()
        view.alpha = 0f

        view.animate()
                .translationX(0f)
                .alpha(1f)
                .setDuration(duration)
                .setInterpolator(DecelerateInterpolator())
                .setListener(
                        object : AnimatorListenerAdapter() {
                            override fun onAnimationEnd(animation: Animator) {
                                onEnd?.invoke()
                            }
                        }
                )
                .start()
    }

    /**
     * Bounce animation - makes view bounce
     * @param view The view to bounce
     */
    fun bounce(view: View) {
        val animator = ObjectAnimator.ofFloat(view, "translationY", 0f, -30f, 0f)
        animator.duration = 500
        animator.interpolator = OvershootInterpolator()
        animator.start()
    }

    /**
     * Reveal animation - circular reveal effect (API 21+)
     * @param view The view to reveal
     * @param centerX Center X coordinate for reveal
     * @param centerY Center Y coordinate for reveal
     * @param duration Animation duration in milliseconds
     */
    fun circularReveal(
            view: View,
            centerX: Int = view.width / 2,
            centerY: Int = view.height / 2,
            duration: Long = DURATION_LONG
    ) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            val finalRadius = Math.hypot(view.width.toDouble(), view.height.toDouble()).toFloat()
            val anim =
                    android.view.ViewAnimationUtils.createCircularReveal(
                            view,
                            centerX,
                            centerY,
                            0f,
                            finalRadius
                    )
            view.visibility = View.VISIBLE
            anim.duration = duration
            anim.start()
        } else {
            // Fallback for older devices
            fadeIn(view, duration)
        }
    }

    /**
     * Elevation animation - animates view elevation
     * @param view The view to animate
     * @param targetElevation Target elevation in dp
     * @param duration Animation duration in milliseconds
     */
    fun animateElevation(view: View, targetElevation: Float, duration: Long = DURATION_MEDIUM) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            val currentElevation = view.elevation
            val animator = ValueAnimator.ofFloat(currentElevation, targetElevation)
            animator.duration = duration
            animator.addUpdateListener { animation ->
                view.elevation = animation.animatedValue as Float
            }
            animator.start()
        }
    }

    /**
     * Add ripple effect to a view programmatically Note: This is typically done in XML, but can be
     * useful for dynamic views
     */
    fun addRippleEffect(view: View) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            val outValue = android.util.TypedValue()
            view.context.theme.resolveAttribute(
                    android.R.attr.selectableItemBackground,
                    outValue,
                    true
            )
            view.setBackgroundResource(outValue.resourceId)
        }
    }

    /** Add circular ripple effect to a view */
    fun addCircularRippleEffect(view: View) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            val outValue = android.util.TypedValue()
            view.context.theme.resolveAttribute(
                    android.R.attr.selectableItemBackgroundBorderless,
                    outValue,
                    true
            )
            view.setBackgroundResource(outValue.resourceId)
        }
    }

    /**
     * Animate view height change
     * @param view The view to animate
     * @param targetHeight Target height in pixels
     * @param duration Animation duration in milliseconds
     */
    fun animateHeight(view: View, targetHeight: Int, duration: Long = DURATION_MEDIUM) {
        val initialHeight = view.height
        val animator = ValueAnimator.ofInt(initialHeight, targetHeight)
        animator.duration = duration
        animator.addUpdateListener { animation ->
            val layoutParams = view.layoutParams
            layoutParams.height = animation.animatedValue as Int
            view.layoutParams = layoutParams
        }
        animator.start()
    }

    /**
     * Animate view width change
     * @param view The view to animate
     * @param targetWidth Target width in pixels
     * @param duration Animation duration in milliseconds
     */
    fun animateWidth(view: View, targetWidth: Int, duration: Long = DURATION_MEDIUM) {
        val initialWidth = view.width
        val animator = ValueAnimator.ofInt(initialWidth, targetWidth)
        animator.duration = duration
        animator.addUpdateListener { animation ->
            val layoutParams = view.layoutParams
            layoutParams.width = animation.animatedValue as Int
            view.layoutParams = layoutParams
        }
        animator.start()
    }
}
