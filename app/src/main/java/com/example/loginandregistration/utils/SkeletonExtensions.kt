package com.example.loginandregistration.utils

import android.view.View
import android.view.ViewGroup
import com.example.loginandregistration.views.SkeletonLoader

/** Extension functions for SkeletonLoader to simplify usage */

/** Set corner radius for circular skeleton loaders (like profile pictures) */
fun View.setCircularSkeleton() {
    if (this is SkeletonLoader) {
        post {
            val radius = (width / 2).toFloat()
            setCornerRadius(radius)
        }
    }
}

/** Set corner radius for rounded skeleton loaders (like chips) */
fun View.setRoundedSkeleton(radiusDp: Float = 12f) {
    if (this is SkeletonLoader) {
        val radiusPx = radiusDp * context.resources.displayMetrics.density
        setCornerRadius(radiusPx)
    }
}

/** Apply circular corner radius to all SkeletonLoader views with specific IDs */
fun ViewGroup.applyCircularSkeletons(vararg viewIds: Int) {
    viewIds.forEach { id -> findViewById<SkeletonLoader>(id)?.setCircularSkeleton() }
}

/** Apply rounded corner radius to all SkeletonLoader views with specific IDs */
fun ViewGroup.applyRoundedSkeletons(radiusDp: Float = 12f, vararg viewIds: Int) {
    viewIds.forEach { id -> findViewById<SkeletonLoader>(id)?.setRoundedSkeleton(radiusDp) }
}
