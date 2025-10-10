package com.example.loginandregistration.utils

import android.view.View
import android.view.ViewGroup
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import androidx.recyclerview.widget.RecyclerView

/**
 * Helper class to manage skeleton loaders in RecyclerViews. Provides methods to show skeleton
 * loading state and transition to actual content.
 */
object SkeletonLoaderHelper {

    private const val FADE_DURATION = 300L

    /** Show skeleton loader and hide the actual content RecyclerView */
    fun showSkeleton(skeletonContainer: ViewGroup, contentRecyclerView: RecyclerView) {
        skeletonContainer.visibility = View.VISIBLE
        contentRecyclerView.visibility = View.GONE
    }

    /** Hide skeleton loader and show the actual content with fade-in animation */
    fun showContent(skeletonContainer: ViewGroup, contentRecyclerView: RecyclerView) {
        // Fade out skeleton
        fadeOut(skeletonContainer) { skeletonContainer.visibility = View.GONE }

        // Fade in content
        contentRecyclerView.visibility = View.VISIBLE
        fadeIn(contentRecyclerView)
    }

    /** Fade in animation for a view */
    fun fadeIn(view: View, duration: Long = FADE_DURATION) {
        val fadeIn =
                AlphaAnimation(0f, 1f).apply {
                    this.duration = duration
                    fillAfter = true
                }
        view.startAnimation(fadeIn)
    }

    /** Fade out animation for a view */
    fun fadeOut(view: View, duration: Long = FADE_DURATION, onEnd: (() -> Unit)? = null) {
        val fadeOut =
                AlphaAnimation(1f, 0f).apply {
                    this.duration = duration
                    fillAfter = true
                    setAnimationListener(
                            object : Animation.AnimationListener {
                                override fun onAnimationStart(animation: Animation?) {}
                                override fun onAnimationRepeat(animation: Animation?) {}
                                override fun onAnimationEnd(animation: Animation?) {
                                    onEnd?.invoke()
                                }
                            }
                    )
                }
        view.startAnimation(fadeOut)
    }

    /** Create a skeleton adapter that shows multiple skeleton items */
    fun createSkeletonAdapter(layoutResId: Int, itemCount: Int = 5): SkeletonAdapter {
        return SkeletonAdapter(layoutResId, itemCount)
    }

    /** Simple adapter to display skeleton loading items */
    class SkeletonAdapter(private val layoutResId: Int, private val itemCount: Int) :
            RecyclerView.Adapter<SkeletonViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SkeletonViewHolder {
            val view =
                    android.view.LayoutInflater.from(parent.context)
                            .inflate(layoutResId, parent, false)

            // Apply circular corner radius to skeleton loaders that need it
            applyCornerRadiusToSkeletons(view as ViewGroup)

            return SkeletonViewHolder(view)
        }

        override fun onBindViewHolder(holder: SkeletonViewHolder, position: Int) {
            // No binding needed for skeleton items
        }

        override fun getItemCount(): Int = itemCount

        private fun applyCornerRadiusToSkeletons(viewGroup: ViewGroup) {
            for (i in 0 until viewGroup.childCount) {
                val child = viewGroup.getChildAt(i)

                when (child.id) {
                    // Profile/avatar skeletons should be circular
                    com.example.loginandregistration.R.id.profileSkeleton -> {
                        (child as? com.example.loginandregistration.views.SkeletonLoader)?.apply {
                            post {
                                val radius = (width / 2).toFloat()
                                setCornerRadius(radius)
                            }
                        }
                    }
                    // Icon skeletons should be circular
                    com.example.loginandregistration.R.id.iconSkeleton -> {
                        (child as? com.example.loginandregistration.views.SkeletonLoader)?.apply {
                            post {
                                val radius = (width / 2).toFloat()
                                setCornerRadius(radius)
                            }
                        }
                    }
                    // Chip skeletons should be rounded
                    com.example.loginandregistration.R.id.chipSkeleton -> {
                        (child as? com.example.loginandregistration.views.SkeletonLoader)?.apply {
                            val radiusPx = 12f * context.resources.displayMetrics.density
                            setCornerRadius(radiusPx)
                        }
                    }
                }

                // Recursively check child ViewGroups
                if (child is ViewGroup) {
                    applyCornerRadiusToSkeletons(child)
                }
            }
        }
    }

    /** ViewHolder for skeleton items */
    class SkeletonViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
}
