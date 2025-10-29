package com.example.loginandregistration.views

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.core.content.ContextCompat
import com.example.loginandregistration.R
import com.example.loginandregistration.models.Message
import com.example.loginandregistration.models.MessageStatus

/**
 * Custom view for displaying message status indicators Shows different icons based on message
 * status:
 * - Sending: Clock icon (white)
 * - Sent: Single checkmark (white)
 * - Delivered: Double gray checkmark
 * - Read: Double blue checkmark
 * - Failed: Error icon (red) - legacy status
 * - Failed Retryable: Error icon (orange) with retry capability
 * - Failed Permanent: Error icon (dark red) - no retry
 */
class MessageStatusView
@JvmOverloads
constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) :
        FrameLayout(context, attrs, defStyleAttr) {

    private val statusIconView: ImageView
    private var onRetryClickListener: (() -> Unit)? = null

    init {
        LayoutInflater.from(context).inflate(R.layout.view_message_status, this, true)
        statusIconView = findViewById(R.id.statusIconView)
    }

    /** Update the status view based on the message */
    fun setMessage(message: Message, currentUserId: String) {
        // Only show status for sent messages
        if (message.senderId != currentUserId) {
            visibility = GONE
            return
        }

        visibility = VISIBLE
        updateStatus(message)
    }

    /** Set retry click listener for failed messages */
    fun setOnRetryClickListener(listener: () -> Unit) {
        onRetryClickListener = listener
    }

    private fun updateStatus(message: Message) {
        when (message.status) {
            MessageStatus.SENDING -> {
                // Sending: show clock icon
                statusIconView.setImageResource(R.drawable.ic_clock)
                statusIconView.setColorFilter(
                        ContextCompat.getColor(context, android.R.color.white)
                )
                statusIconView.alpha = 0.7f
                isClickable = false
                setOnClickListener(null)
            }
            MessageStatus.SENT -> {
                // Sent: single checkmark
                statusIconView.setImageResource(R.drawable.ic_check)
                statusIconView.setColorFilter(
                        ContextCompat.getColor(context, android.R.color.white)
                )
                statusIconView.alpha = 0.7f
                isClickable = false
                setOnClickListener(null)
            }
            MessageStatus.DELIVERED -> {
                // Delivered: double checkmark (gray)
                statusIconView.setImageResource(R.drawable.ic_check_double)
                statusIconView.setColorFilter(
                        ContextCompat.getColor(context, android.R.color.white)
                )
                statusIconView.alpha = 0.7f
                isClickable = false
                setOnClickListener(null)
            }
            MessageStatus.READ -> {
                // Read: double checkmark (blue)
                statusIconView.setImageResource(R.drawable.ic_check_double)
                statusIconView.setColorFilter(
                        ContextCompat.getColor(context, android.R.color.holo_blue_light)
                )
                statusIconView.alpha = 1.0f
                isClickable = false
                setOnClickListener(null)
            }
            MessageStatus.FAILED -> {
                // Failed: error icon with retry capability (legacy)
                statusIconView.setImageResource(R.drawable.ic_error)
                statusIconView.setColorFilter(
                        ContextCompat.getColor(context, android.R.color.holo_red_light)
                )
                statusIconView.alpha = 1.0f
                isClickable = true
                setOnClickListener { onRetryClickListener?.invoke() }
            }
            MessageStatus.FAILED_RETRYABLE -> {
                // Failed but retryable: warning icon with retry capability
                statusIconView.setImageResource(R.drawable.ic_error)
                statusIconView.setColorFilter(
                        ContextCompat.getColor(context, android.R.color.holo_orange_light)
                )
                statusIconView.alpha = 1.0f
                isClickable = true
                setOnClickListener { onRetryClickListener?.invoke() }
            }
            MessageStatus.FAILED_PERMANENT -> {
                // Failed permanently: error icon, no retry
                statusIconView.setImageResource(R.drawable.ic_error)
                statusIconView.setColorFilter(
                        ContextCompat.getColor(context, android.R.color.holo_red_dark)
                )
                statusIconView.alpha = 1.0f
                isClickable = false
                setOnClickListener(null)
            }
        }
    }

    /** Update status in real-time */
    fun updateStatus(status: MessageStatus, readByCount: Int = 0) {
        val message = Message(status = status, readBy = List(readByCount) { "" })
        updateStatus(message)
    }
}
