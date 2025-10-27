package com.example.loginandregistration.utils

import android.view.View

/** Extension functions for View animations and effects */

/** Add ripple effect to any view */
fun View.addRippleEffect() {
    AnimationUtils.addRippleEffect(this)
}

/** Add circular ripple effect to any view (borderless) */
fun View.addCircularRippleEffect() {
    AnimationUtils.addCircularRippleEffect(this)
}

/** Animate button press effect */
fun View.animatePress() {
    AnimationUtils.buttonPress(this)
}

/** Fade in the view */
fun View.fadeIn(duration: Long = 300, onEnd: (() -> Unit)? = null) {
    AnimationUtils.fadeIn(this, duration, onEnd)
}

/** Fade out the view */
fun View.fadeOut(duration: Long = 300, onEnd: (() -> Unit)? = null) {
    AnimationUtils.fadeOut(this, duration, onEnd)
}

/** Slide up the view */
fun View.slideUp(duration: Long = 300, onEnd: (() -> Unit)? = null) {
    AnimationUtils.slideUp(this, duration, onEnd)
}

/** Slide down the view */
fun View.slideDown(duration: Long = 300, onEnd: (() -> Unit)? = null) {
    AnimationUtils.slideDown(this, duration, onEnd)
}

/** Scale in the view */
fun View.scaleIn(duration: Long = 200, onEnd: (() -> Unit)? = null) {
    AnimationUtils.scaleIn(this, duration, onEnd)
}

/** Scale out the view */
fun View.scaleOut(duration: Long = 200, onEnd: (() -> Unit)? = null) {
    AnimationUtils.scaleOut(this, duration, onEnd)
}

/** Shake the view (for error feedback) */
fun View.shake() {
    AnimationUtils.shake(this)
}

/** Pulse the view */
fun View.pulse() {
    AnimationUtils.pulse(this)
}

/** Bounce the view */
fun View.bounce() {
    AnimationUtils.bounce(this)
}
