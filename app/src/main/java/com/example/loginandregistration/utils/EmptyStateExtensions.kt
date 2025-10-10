package com.example.loginandregistration.utils

import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.example.loginandregistration.views.EmptyStateView

/** Extension functions for managing empty states in lists */

/**
 * Show or hide empty state based on adapter item count
 * @param emptyStateView The EmptyStateView to show/hide
 * @param configureEmptyState Lambda to configure the empty state when shown
 */
fun RecyclerView.manageEmptyState(
        emptyStateView: EmptyStateView,
        configureEmptyState: EmptyStateView.() -> Unit
) {
    val checkEmpty = {
        val isEmpty = adapter?.itemCount == 0
        emptyStateView.isVisible = isEmpty
        this.isVisible = !isEmpty

        if (isEmpty) {
            configureEmptyState(emptyStateView)
        }
    }

    // Check initially
    checkEmpty()

    // Observe adapter changes
    adapter?.registerAdapterDataObserver(
            object : RecyclerView.AdapterDataObserver() {
                override fun onChanged() {
                    checkEmpty()
                }

                override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                    checkEmpty()
                }

                override fun onItemRangeRemoved(positionStart: Int, itemCount: Int) {
                    checkEmpty()
                }
            }
    )
}

/** Show empty state and hide content */
fun View.showEmptyState(emptyStateView: EmptyStateView, configure: EmptyStateView.() -> Unit) {
    this.isVisible = false
    emptyStateView.isVisible = true
    configure(emptyStateView)
}

/** Hide empty state and show content */
fun View.hideEmptyState(emptyStateView: EmptyStateView) {
    this.isVisible = true
    emptyStateView.isVisible = false
}

/** Toggle between content and empty state based on condition */
fun View.toggleEmptyState(
        emptyStateView: EmptyStateView,
        isEmpty: Boolean,
        configure: EmptyStateView.() -> Unit
) {
    if (isEmpty) {
        showEmptyState(emptyStateView, configure)
    } else {
        hideEmptyState(emptyStateView)
    }
}

/** Add an EmptyStateView to a ViewGroup programmatically */
fun ViewGroup.addEmptyStateView(): EmptyStateView {
    val emptyStateView = EmptyStateView(context)
    emptyStateView.isVisible = false
    addView(
            emptyStateView,
            ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
            )
    )
    return emptyStateView
}
