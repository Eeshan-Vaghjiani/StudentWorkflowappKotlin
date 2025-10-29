package com.example.loginandregistration

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.loginandregistration.adapters.NotificationAdapter
import com.example.loginandregistration.models.Notification
import com.example.loginandregistration.models.NotificationType
import com.example.loginandregistration.utils.ErrorStateManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class NotificationsFragment : Fragment() {

    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var errorStateManager: ErrorStateManager

    private var notificationsRecyclerView: RecyclerView? = null
    private var emptyStateLayout: View? = null
    private var loadingIndicator: ProgressBar? = null
    private var notificationAdapter: NotificationAdapter? = null

    companion object {
        private const val TAG = "NotificationsFragment"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = Firebase.auth
        firestore = FirebaseFirestore.getInstance()
        errorStateManager = ErrorStateManager(requireContext())
    }

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_notifications, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupToolbar(view)
        initViews(view)
        setupRecyclerView()
        loadNotifications()
    }

    private fun setupToolbar(view: View) {
        val toolbar =
                view.findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbarNotifications)
        (activity as? AppCompatActivity)?.setSupportActionBar(toolbar)
        (activity as? AppCompatActivity)?.supportActionBar?.title =
                getString(R.string.menu_notifications)
    }

    private fun initViews(view: View) {
        notificationsRecyclerView = view.findViewById(R.id.notificationsRecyclerView)
        emptyStateLayout = view.findViewById(R.id.emptyStateLayout)
        loadingIndicator = view.findViewById(R.id.loadingIndicator)
    }

    private fun setupRecyclerView() {
        notificationAdapter = NotificationAdapter { notification ->
            handleNotificationClick(notification)
        }

        notificationsRecyclerView?.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = notificationAdapter
        }
    }

    private fun loadNotifications() {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            Log.w(TAG, "User not authenticated")
            showEmptyState()
            return
        }

        showLoadingState()

        lifecycleScope.launch {
            try {
                val notifications = fetchNotifications(userId)

                if (notifications.isEmpty()) {
                    showEmptyState()
                } else {
                    showNotifications(notifications)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error loading notifications", e)
                showErrorState(e)
            }
        }
    }

    private suspend fun fetchNotifications(userId: String): List<Notification> {
        return try {
            val snapshot =
                    firestore
                            .collection("notifications")
                            .whereEqualTo("userId", userId)
                            .orderBy("timestamp", Query.Direction.DESCENDING)
                            .limit(50)
                            .get()
                            .await()

            snapshot.documents.mapNotNull { doc ->
                try {
                    doc.toObject(Notification::class.java)
                } catch (e: Exception) {
                    Log.e(TAG, "Error parsing notification: ${doc.id}", e)
                    null
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching notifications", e)
            throw e
        }
    }

    private fun handleNotificationClick(notification: Notification) {
        // Mark as read
        markNotificationAsRead(notification.id)

        // Navigate based on notification type
        when (notification.type) {
            NotificationType.TASK_ASSIGNED,
            NotificationType.TASK_COMPLETED,
            NotificationType.TASK_DUE_SOON -> {
                notification.taskId?.let { taskId -> navigateToTask(taskId) }
            }
            NotificationType.GROUP_INVITE, NotificationType.GROUP_JOINED -> {
                notification.groupId?.let { groupId -> navigateToGroup(groupId) }
            }
            NotificationType.NEW_MESSAGE, NotificationType.MENTION -> {
                notification.chatId?.let { chatId -> navigateToChat(chatId) }
            }
            NotificationType.SYSTEM -> {
                // System notifications don't navigate anywhere
            }
        }
    }

    private fun markNotificationAsRead(notificationId: String) {
        lifecycleScope.launch {
            try {
                firestore
                        .collection("notifications")
                        .document(notificationId)
                        .update("isRead", true)
                        .await()

                Log.d(TAG, "Notification marked as read: $notificationId")
            } catch (e: Exception) {
                Log.e(TAG, "Error marking notification as read", e)
            }
        }
    }

    private fun navigateToTask(taskId: String) {
        val intent =
                Intent(requireContext(), TaskDetailsActivity::class.java).apply {
                    putExtra(TaskDetailsActivity.EXTRA_TASK_ID, taskId)
                }
        startActivity(intent)
    }

    private fun navigateToGroup(groupId: String) {
        val intent =
                Intent(requireContext(), GroupDetailsActivity::class.java).apply {
                    putExtra("groupId", groupId)
                }
        startActivity(intent)
    }

    private fun navigateToChat(chatId: String) {
        val intent =
                Intent(requireContext(), ChatRoomActivity::class.java).apply {
                    putExtra("chatId", chatId)
                    putExtra("chatName", "Chat")
                }
        startActivity(intent)
    }

    private fun showLoadingState() {
        loadingIndicator?.visibility = View.VISIBLE
        notificationsRecyclerView?.visibility = View.GONE
        emptyStateLayout?.visibility = View.GONE
    }

    private fun showNotifications(notifications: List<Notification>) {
        loadingIndicator?.visibility = View.GONE
        notificationsRecyclerView?.visibility = View.VISIBLE
        emptyStateLayout?.visibility = View.GONE

        notificationAdapter?.submitList(notifications)
    }

    private fun showEmptyState() {
        loadingIndicator?.visibility = View.GONE
        notificationsRecyclerView?.visibility = View.GONE
        emptyStateLayout?.visibility = View.VISIBLE

        // Configure empty state
        emptyStateLayout
                ?.findViewById<ImageView>(R.id.emptyStateIcon)
                ?.setImageResource(android.R.drawable.ic_popup_reminder)
        emptyStateLayout?.findViewById<TextView>(R.id.emptyStateTitle)?.text =
                getString(R.string.empty_state_no_notifications_title)
        emptyStateLayout?.findViewById<TextView>(R.id.emptyStateDescription)?.text =
                getString(R.string.empty_state_no_notifications_description)
    }

    private fun showErrorState(exception: Throwable) {
        loadingIndicator?.visibility = View.GONE
        notificationsRecyclerView?.visibility = View.GONE

        val errorState = errorStateManager.categorizeError(exception)
        view?.let { v -> errorStateManager.showError(errorState, v) { loadNotifications() } }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        notificationsRecyclerView = null
        emptyStateLayout = null
        loadingIndicator = null
        notificationAdapter = null
    }
}
