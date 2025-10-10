package com.example.loginandregistration.views

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Shader
import android.util.AttributeSet
import android.view.View
import android.view.animation.LinearInterpolator
import androidx.core.content.ContextCompat
import com.example.loginandregistration.R

/**
 * Custom view that displays a shimmer loading effect for skeleton screens. This view creates an
 * animated gradient that moves across the view to indicate loading.
 */
class SkeletonLoader
@JvmOverloads
constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) :
        View(context, attrs, defStyleAttr) {

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val rect = RectF()
    private var shimmerAnimator: ValueAnimator? = null
    private var gradientOffset = 0f

    private val baseColor: Int
    private val shimmerColor: Int
    private var cornerRadius: Float = 8f

    init {
        // Use default colors
        baseColor = ContextCompat.getColor(context, R.color.skeleton_base)
        shimmerColor = ContextCompat.getColor(context, R.color.skeleton_shimmer)

        // Read corner radius from layout if provided (using android:radius attribute)
        attrs?.let {
            val typedArray = context.obtainStyledAttributes(it, intArrayOf(android.R.attr.radius))
            cornerRadius = typedArray.getDimension(0, 8f)
            typedArray.recycle()
        }

        setupShimmerAnimation()
    }

    /** Set the corner radius programmatically */
    fun setCornerRadius(radius: Float) {
        cornerRadius = radius
        invalidate()
    }

    private fun setupShimmerAnimation() {
        shimmerAnimator =
                ValueAnimator.ofFloat(0f, 1f).apply {
                    duration = 1500
                    repeatCount = ValueAnimator.INFINITE
                    interpolator = LinearInterpolator()
                    addUpdateListener { animation ->
                        gradientOffset = animation.animatedValue as Float
                        invalidate()
                    }
                }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        rect.set(0f, 0f, w.toFloat(), h.toFloat())
        updateShader()
    }

    private fun updateShader() {
        if (width == 0 || height == 0) return

        val shimmerWidth = width * 0.3f
        val gradientStart = -shimmerWidth + (width + shimmerWidth) * gradientOffset

        paint.shader =
                LinearGradient(
                        gradientStart,
                        0f,
                        gradientStart + shimmerWidth,
                        0f,
                        intArrayOf(baseColor, shimmerColor, baseColor),
                        floatArrayOf(0f, 0.5f, 1f),
                        Shader.TileMode.CLAMP
                )
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        updateShader()
        canvas.drawRoundRect(rect, cornerRadius, cornerRadius, paint)
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        startShimmer()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        stopShimmer()
    }

    /** Start the shimmer animation */
    fun startShimmer() {
        shimmerAnimator?.start()
    }

    /** Stop the shimmer animation */
    fun stopShimmer() {
        shimmerAnimator?.cancel()
    }

    override fun onVisibilityChanged(changedView: View, visibility: Int) {
        super.onVisibilityChanged(changedView, visibility)
        if (visibility == VISIBLE) {
            startShimmer()
        } else {
            stopShimmer()
        }
    }
}
