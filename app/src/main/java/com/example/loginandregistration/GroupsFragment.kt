package com.example.loginandregistration

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.loginandregistration.databinding.FragmentGroupsBinding
import com.example.loginandregistration.models.*
import com.example.loginandregistration.repository.GroupRepository
import com.example.loginandregistration.utils.ConnectionMonitor
import com.example.loginandregistration.utils.ErrorHandler
import com.example.loginandregistration.utils.collectWithLifecycle
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.Timestamp
import java.text.SimpleDateFormat
import java.util.Locale
import kotlinx.coroutines.launch

class GroupsFragment : Fragment() {

        companion object {
                private const val TAG = "GroupsFragment"
        }

        private var _binding: FragmentGroupsBinding? = null
        private val binding: FragmentGroupsBinding?
                get() = _binding

        private lateinit var myGroupsAdapter: GroupAdapter
        private lateinit var activityAdapter: ActivityAdapter
        private lateinit var discoverGroupsAdapter: DiscoverGroupAdapter

        private lateinit var groupRepository: GroupRepository
        private lateinit var connectionMonitor: ConnectionMonitor
        private var isLoading = false

        override fun onCreateView(
                inflater: LayoutInflater,
                container: ViewGroup?,
                savedInstanceState: Bundle?
        ): View {
                _binding = FragmentGroupsBinding.inflate(inflater, container, false)

                groupRepository = GroupRepository()
                connectionMonitor = ConnectionMonitor(requireContext())
                setupViews()
                setupRecyclerViews()
                setupClickListeners()
                setupConnectionMonitoring()
                setupRealTimeListeners()

                return _binding!!.root
        }

        override fun onDestroyView() {
                super.onDestroyView()
                Log.d(TAG, "onDestroyView - Clearing binding reference")
                _binding = null
        }

        private fun setupViews() {
                val binding = _binding ?: return

                // Setup swipe refresh
                binding.swipeRefreshLayout.setOnRefreshListener { refreshData() }

                // Show loading initially
                showLoading(true)
        }

        private fun setupConnectionMonitoring() {
                connectionMonitor.isConnected.collectWithLifecycle(viewLifecycleOwner) { isConnected
                        ->
                        val binding = _binding ?: return@collectWithLifecycle

                        // Find the offline indicator from the root view
                        binding.root.findViewById<View>(R.id.offlineIndicator)?.visibility =
                                if (isConnected) View.GONE else View.VISIBLE

                        if (!isConnected) {
                                Log.w(TAG, "Device is offline")
                        } else {
                                Log.d(TAG, "Device is online")
                        }
                }
        }

        private fun showLoading(show: Boolean) {
                val binding =
                        _binding
                                ?: run {
                                        Log.d(TAG, "Cannot show loading state: view is destroyed")
                                        return
                                }

                isLoading = show
                binding.swipeRefreshLayout.isRefreshing = show
        }

        private fun setupRecyclerViews() {
                val binding = _binding ?: return

                // My Groups - Initialize with click listener, will be populated by real-time
                // listener
                myGroupsAdapter = GroupAdapter { group ->
                        Toast.makeText(context, "Clicked: ${group.name}", Toast.LENGTH_SHORT).show()
                }

                binding.recyclerMyGroups.apply {
                        layoutManager = LinearLayoutManager(context)
                        adapter = myGroupsAdapter
                }

                // Recent Activity - Initialize with click listener, will be populated from
                // Firestore
                activityAdapter = ActivityAdapter { _ ->
                        Toast.makeText(context, "Activity clicked", Toast.LENGTH_SHORT).show()
                }

                binding.recyclerRecentActivity.apply {
                        layoutManager = LinearLayoutManager(context)
                        adapter = activityAdapter
                }

                // Discover Groups - Initialize with empty list, will be populated from Firestore
                discoverGroupsAdapter =
                        DiscoverGroupAdapter(emptyList()) { group -> joinDiscoverGroup(group) }

                binding.recyclerDiscoverGroups.apply {
                        layoutManager = LinearLayoutManager(context)
                        adapter = discoverGroupsAdapter
                }
        }

        private fun setupClickListeners() {
                val binding = _binding ?: return

                // Header buttons
                binding.btnSearchGroups.setOnClickListener {
                        Toast.makeText(
                                        context,
                                        getString(R.string.search_clicked),
                                        Toast.LENGTH_SHORT
                                )
                                .show()
                }

                binding.btnJoinGroup.setOnClickListener { showJoinGroupDialog() }

                // Quick action buttons
                binding.btnCreateGroup.setOnClickListener { showCreateGroupDialog() }

                binding.btnJoinGroupAction.setOnClickListener { showJoinGroupDialog() }

                binding.btnAssignments.setOnClickListener {
                        Toast.makeText(context, "Assignments clicked", Toast.LENGTH_SHORT).show()
                }

                binding.btnGroupChat.setOnClickListener {
                        Toast.makeText(context, "Group chat clicked", Toast.LENGTH_SHORT).show()
                }

                // Navigation buttons
                binding.btnShowAllGroups.setOnClickListener {
                        Toast.makeText(context, "Show all groups clicked", Toast.LENGTH_SHORT)
                                .show()
                }

                binding.btnShowMoreGroups.setOnClickListener {
                        Toast.makeText(context, "Show more groups clicked", Toast.LENGTH_SHORT)
                                .show()
                }
        }

        private fun setupRealTimeListeners() {
                // Use lifecycle-aware collection that automatically stops when Fragment is stopped
                // and resumes when Fragment is started again

                // Real-time listener for group statistics
                lifecycleScope.launch {
                        try {
                                groupRepository.getGroupStatsFlow().collectWithLifecycle(
                                                viewLifecycleOwner
                                        ) { stats ->
                                        try {
                                                Log.d(
                                                        TAG,
                                                        "Received group stats update: myGroups=${stats.myGroups}, activeAssignments=${stats.activeAssignments}, newMessages=${stats.newMessages}"
                                                )

                                                val binding =
                                                        _binding
                                                                ?: run {
                                                                        Log.d(
                                                                                TAG,
                                                                                "Cannot update group stats: view is destroyed"
                                                                        )
                                                                        return@collectWithLifecycle
                                                                }

                                                binding.tvMyGroupsCount.text =
                                                        stats.myGroups.toString()
                                                binding.tvActiveAssignmentsCount.text =
                                                        stats.activeAssignments.toString()
                                                binding.tvNewMessagesCount.text =
                                                        stats.newMessages.toString()

                                                Log.d(
                                                        TAG,
                                                        "Updated UI elements: myGroups=${binding.tvMyGroupsCount.text}, activeAssignments=${binding.tvActiveAssignmentsCount.text}, newMessages=${binding.tvNewMessagesCount.text}"
                                                )
                                        } catch (e: Exception) {
                                                Log.e(TAG, "Error processing group stats update", e)
                                                // Don't crash, just log the error
                                        }
                                }
                        } catch (e: Exception) {
                                Log.e(TAG, "Error collecting group stats", e)
                                handleGroupsError(e)
                        }
                }

                // Real-time listener for user's groups
                lifecycleScope.launch {
                        try {
                                groupRepository.getUserGroupsFlow().collectWithLifecycle(
                                                viewLifecycleOwner
                                        ) { firebaseGroups ->
                                        try {
                                                Log.d(
                                                        TAG,
                                                        "Received user groups update: ${firebaseGroups.size} groups"
                                                )

                                                val binding =
                                                        _binding
                                                                ?: run {
                                                                        Log.d(
                                                                                TAG,
                                                                                "Cannot update user groups: view is destroyed"
                                                                        )
                                                                        return@collectWithLifecycle
                                                                }

                                                // Hide loading skeleton after first data load
                                                if (isLoading) {
                                                        showLoading(false)
                                                }

                                                // Stop refresh animation
                                                binding.swipeRefreshLayout.isRefreshing = false

                                                // Update empty state visibility
                                                updateMyGroupsEmptyState(firebaseGroups.isEmpty())

                                                val displayGroups =
                                                        firebaseGroups.map { firebaseGroup ->
                                                                Group(
                                                                        id =
                                                                                firebaseGroup.id
                                                                                        .hashCode(),
                                                                        name = firebaseGroup.name,
                                                                        details =
                                                                                "${firebaseGroup.members.size} members • ${firebaseGroup.subject}",
                                                                        assignmentCount =
                                                                                firebaseGroup
                                                                                        .tasks
                                                                                        .size,
                                                                        iconColor = "#007AFF",
                                                                        iconResource =
                                                                                R.drawable.ic_groups
                                                                )
                                                        }

                                                Log.d(
                                                        TAG,
                                                        "Mapped to ${displayGroups.size} display groups"
                                                )

                                                myGroupsAdapter = GroupAdapter { group ->
                                                        // Navigate to group details
                                                        val intent =
                                                                Intent(
                                                                        context,
                                                                        GroupDetailsActivity::class
                                                                                .java
                                                                )
                                                        intent.putExtra(
                                                                "GROUP_ID",
                                                                firebaseGroups
                                                                        .find {
                                                                                it.name ==
                                                                                        group.name
                                                                        }
                                                                        ?.id
                                                                        ?: ""
                                                        )
                                                        startActivity(intent)
                                                }
                                                myGroupsAdapter.submitList(displayGroups)
                                                _binding?.recyclerMyGroups?.adapter =
                                                        myGroupsAdapter
                                                Log.d(
                                                        TAG,
                                                        "Updated recyclerMyGroups adapter with ${myGroupsAdapter.itemCount} items"
                                                )
                                        } catch (e: Exception) {
                                                Log.e(TAG, "Error processing user groups update", e)
                                                if (_binding != null && isAdded) {
                                                        handleGroupsError(e)
                                                        showLoading(false)
                                                        _binding?.swipeRefreshLayout?.isRefreshing =
                                                                false
                                                        updateMyGroupsEmptyState(true)
                                                } else {
                                                        Log.d(
                                                                TAG,
                                                                "View destroyed, skipping error UI update"
                                                        )
                                                }
                                        }
                                }
                        } catch (e: Exception) {
                                Log.e(TAG, "Error collecting user groups", e)
                                if (_binding != null && isAdded) {
                                        handleGroupsError(e)
                                        showLoading(false)
                                        _binding?.swipeRefreshLayout?.isRefreshing = false
                                        updateMyGroupsEmptyState(true)
                                } else {
                                        Log.d(TAG, "View destroyed, skipping error UI update")
                                }
                        }
                }

                // Load initial data for activities and discover groups
                loadInitialData()
        }

        private fun loadInitialData() {
                lifecycleScope.launch {
                        try {
                                // Load recent activities
                                val activitiesResult = groupRepository.getGroupActivities()
                                activitiesResult.fold(
                                        onSuccess = { activities ->
                                                val displayActivities =
                                                        activities.map { activity ->
                                                                Activity(
                                                                        id = activity.id.hashCode(),
                                                                        title = activity.title,
                                                                        details =
                                                                                "${activity.description} • ${formatTimestamp(activity.createdAt)}",
                                                                        iconColor = "#007AFF",
                                                                        iconResource =
                                                                                getActivityIcon(
                                                                                        activity.type
                                                                                )
                                                                )
                                                        }

                                                activityAdapter = ActivityAdapter { _ ->
                                                        Toast.makeText(
                                                                        context,
                                                                        "Activity clicked",
                                                                        Toast.LENGTH_SHORT
                                                                )
                                                                .show()
                                                }
                                                activityAdapter.submitList(displayActivities)
                                                _binding?.recyclerRecentActivity?.adapter =
                                                        activityAdapter

                                                // Update activity empty state
                                                updateActivityEmptyState(
                                                        displayActivities.isEmpty()
                                                )
                                        },
                                        onFailure = { exception ->
                                                Log.e(TAG, "Error loading activities", exception)
                                                // Show empty state for activities
                                                updateActivityEmptyState(true)
                                        }
                                )

                                // Load discoverable groups
                                val publicGroupsResult = groupRepository.getPublicGroups()
                                publicGroupsResult.fold(
                                        onSuccess = { publicGroups ->
                                                val displayDiscoverGroups =
                                                        publicGroups.map { firebaseGroup ->
                                                                DiscoverGroup(
                                                                        id =
                                                                                firebaseGroup.id
                                                                                        .hashCode(),
                                                                        name = firebaseGroup.name,
                                                                        details =
                                                                                "${firebaseGroup.members.size} members • ${firebaseGroup.subject}",
                                                                        iconColor = "#5856D6",
                                                                        iconResource =
                                                                                R.drawable.ic_groups
                                                                )
                                                        }

                                                discoverGroupsAdapter =
                                                        DiscoverGroupAdapter(
                                                                displayDiscoverGroups
                                                        ) { group -> joinDiscoverGroup(group) }
                                                _binding?.recyclerDiscoverGroups?.adapter =
                                                        discoverGroupsAdapter

                                                // Update discover groups empty state
                                                updateDiscoverGroupsEmptyState(
                                                        displayDiscoverGroups.isEmpty()
                                                )
                                        },
                                        onFailure = { exception ->
                                                Log.e(TAG, "Error loading public groups", exception)
                                                // Show empty state for discover groups
                                                updateDiscoverGroupsEmptyState(true)
                                        }
                                )

                                // Hide refresh indicator when data loads
                                _binding?.swipeRefreshLayout?.isRefreshing = false
                        } catch (e: Exception) {
                                Log.e(TAG, "Error loading data", e)
                                _binding?.swipeRefreshLayout?.isRefreshing = false
                                showLoading(false)

                                // Use ErrorHandler for consistent error handling
                                if (_binding != null && isAdded) {
                                        context?.let { ctx ->
                                                ErrorHandler.handleError(ctx, e, _binding?.root) {
                                                        // Retry callback
                                                        loadInitialData()
                                                }
                                        }
                                } else {
                                        Log.d(TAG, "View destroyed, skipping error UI update")
                                }
                        }
                }
        }

        private fun refreshData() {
                val binding = _binding ?: return

                // Show loading indicator
                binding.swipeRefreshLayout.isRefreshing = true

                // Reload all data
                loadInitialData()

                // The real-time listeners will automatically update the data
                // and hide the refresh indicator when complete
        }

        private fun showCreateGroupDialog() {
                val dialogView =
                        LayoutInflater.from(context).inflate(R.layout.dialog_create_group, null)
                val dialog = AlertDialog.Builder(requireContext()).setView(dialogView).create()

                val etGroupName = dialogView.findViewById<TextInputEditText>(R.id.et_group_name)
                val etGroupDescription =
                        dialogView.findViewById<TextInputEditText>(R.id.et_group_description)
                val etGroupSubject =
                        dialogView.findViewById<TextInputEditText>(R.id.et_group_subject)
                val btnCancel = dialogView.findViewById<MaterialButton>(R.id.btn_cancel)
                val btnCreate = dialogView.findViewById<MaterialButton>(R.id.btn_create)

                btnCancel.setOnClickListener { dialog.dismiss() }

                btnCreate.setOnClickListener {
                        val name = etGroupName.text.toString().trim()
                        val description = etGroupDescription.text.toString().trim()
                        val subject = etGroupSubject.text.toString().trim()

                        if (name.isEmpty()) {
                                Toast.makeText(
                                                context,
                                                getString(R.string.fill_required_fields),
                                                Toast.LENGTH_SHORT
                                        )
                                        .show()
                                return@setOnClickListener
                        }

                        lifecycleScope.launch {
                                try {
                                        val result =
                                                groupRepository.createGroup(
                                                        name,
                                                        description,
                                                        subject,
                                                        "public"
                                                )
                                        result.fold(
                                                onSuccess = { groupId ->
                                                        context?.let { ctx ->
                                                                ErrorHandler.showSuccessMessage(
                                                                        ctx,
                                                                        _binding?.root,
                                                                        getString(
                                                                                R.string
                                                                                        .group_created
                                                                        )
                                                                )
                                                        }
                                                        dialog.dismiss()
                                                        // Real-time listener will automatically
                                                        // update the groups list
                                                },
                                                onFailure = { exception ->
                                                        Log.e(
                                                                TAG,
                                                                "Error creating group",
                                                                exception
                                                        )
                                                        context?.let { ctx ->
                                                                ErrorHandler.handleError(
                                                                        ctx,
                                                                        exception,
                                                                        _binding?.root
                                                                ) {
                                                                        // Retry callback
                                                                        btnCreate.performClick()
                                                                }
                                                        }
                                                }
                                        )
                                } catch (e: Exception) {
                                        Log.e(TAG, "Error creating group", e)
                                        context?.let { ctx ->
                                                ErrorHandler.handleError(ctx, e, _binding?.root) {
                                                        // Retry callback
                                                        btnCreate.performClick()
                                                }
                                        }
                                }
                        }
                }

                dialog.show()
        }

        private fun showJoinGroupDialog() {
                val dialogView =
                        LayoutInflater.from(context).inflate(R.layout.dialog_join_group, null)
                val dialog = AlertDialog.Builder(requireContext()).setView(dialogView).create()

                val etGroupCode = dialogView.findViewById<TextInputEditText>(R.id.et_group_code)
                val btnCancel = dialogView.findViewById<MaterialButton>(R.id.btn_cancel)
                val btnJoin = dialogView.findViewById<MaterialButton>(R.id.btn_join)

                btnCancel.setOnClickListener { dialog.dismiss() }

                btnJoin.setOnClickListener {
                        val code = etGroupCode.text.toString().trim().uppercase()

                        if (code.length != 6) {
                                Toast.makeText(
                                                context,
                                                getString(R.string.invalid_group_code),
                                                Toast.LENGTH_SHORT
                                        )
                                        .show()
                                return@setOnClickListener
                        }

                        lifecycleScope.launch {
                                try {
                                        val success = groupRepository.joinGroupByCode(code)
                                        if (success) {
                                                context?.let { ctx ->
                                                        ErrorHandler.showSuccessMessage(
                                                                ctx,
                                                                _binding?.root,
                                                                getString(R.string.group_joined)
                                                        )
                                                }
                                                dialog.dismiss()
                                                // Real-time listener will automatically update the
                                                // groups list
                                        } else {
                                                context?.let { ctx ->
                                                        ErrorHandler.handleGenericError(
                                                                ctx,
                                                                _binding?.root,
                                                                getString(
                                                                        R.string.error_joining_group
                                                                )
                                                        )
                                                }
                                        }
                                } catch (e: Exception) {
                                        Log.e(TAG, "Error joining group", e)
                                        context?.let { ctx ->
                                                ErrorHandler.handleError(ctx, e, _binding?.root) {
                                                        // Retry callback
                                                        btnJoin.performClick()
                                                }
                                        }
                                }
                        }
                }

                dialog.show()
        }

        private fun joinDiscoverGroup(group: DiscoverGroup) {
                lifecycleScope.launch {
                        try {
                                // Find the actual Firebase group by name (this is a simplified
                                // approach)
                                val publicGroupsResult = groupRepository.getPublicGroups()
                                publicGroupsResult.fold(
                                        onSuccess = { publicGroups ->
                                                val firebaseGroup =
                                                        publicGroups.find { it.name == group.name }

                                                if (firebaseGroup != null) {
                                                        val success =
                                                                groupRepository.joinGroup(
                                                                        firebaseGroup.id
                                                                )
                                                        if (success) {
                                                                context?.let { ctx ->
                                                                        ErrorHandler
                                                                                .showSuccessMessage(
                                                                                        ctx,
                                                                                        _binding?.root,
                                                                                        getString(
                                                                                                R.string
                                                                                                        .discover_group_joined,
                                                                                                group.name
                                                                                        )
                                                                                )
                                                                }
                                                                // Real-time listener will
                                                                // automatically update the groups
                                                                // list
                                                        } else {
                                                                context?.let { ctx ->
                                                                        ErrorHandler
                                                                                .handleGenericError(
                                                                                        ctx,
                                                                                        _binding?.root,
                                                                                        getString(
                                                                                                R.string
                                                                                                        .error_joining_group
                                                                                        )
                                                                                )
                                                                }
                                                        }
                                                } else {
                                                        context?.let { ctx ->
                                                                ErrorHandler.handleGenericError(
                                                                        ctx,
                                                                        _binding?.root,
                                                                        "Group not found"
                                                                )
                                                        }
                                                }
                                        },
                                        onFailure = { exception ->
                                                Log.e(TAG, "Error loading public groups", exception)
                                                context?.let { ctx ->
                                                        ErrorHandler.handleError(
                                                                ctx,
                                                                exception,
                                                                _binding?.root
                                                        ) {
                                                                // Retry callback
                                                                joinDiscoverGroup(group)
                                                        }
                                                }
                                        }
                                )
                        } catch (e: Exception) {
                                Log.e(TAG, "Error joining group", e)
                                context?.let { ctx ->
                                        ErrorHandler.handleError(ctx, e, _binding?.root) {
                                                // Retry callback
                                                joinDiscoverGroup(group)
                                        }
                                }
                        }
                }
        }

        private fun getActivityIcon(type: String): Int {
                return when (type) {
                        "message" -> R.drawable.ic_chat
                        "assignment", "task_created", "task_completed" -> R.drawable.ic_assignment
                        "member_joined", "member_left" -> R.drawable.ic_person
                        "group_created" -> R.drawable.ic_groups
                        else -> R.drawable.ic_groups
                }
        }

        private fun formatTimestamp(timestamp: com.google.firebase.Timestamp): String {
                val now = System.currentTimeMillis()
                val time = timestamp.toDate().time
                val diff = now - time

                return when {
                        diff < 60 * 1000 -> "Just now"
                        diff < 60 * 60 * 1000 -> "${diff / (60 * 1000)} min ago"
                        diff < 24 * 60 * 60 * 1000 -> "${diff / (60 * 60 * 1000)} hours ago"
                        diff < 7 * 24 * 60 * 60 * 1000 -> "${diff / (24 * 60 * 60 * 1000)} days ago"
                        else ->
                                SimpleDateFormat("MMM d", Locale.getDefault())
                                        .format(timestamp.toDate())
                }
        }
        private fun loadGroupsData() {
                lifecycleScope.launch {
                        try {
                                // Reload activities and discover groups
                                loadInitialData()

                                // Reload user's groups
                                val result = groupRepository.getUserGroups()
                                result.fold(
                                        onSuccess = { firebaseGroups ->
                                                val displayGroups =
                                                        firebaseGroups.map { firebaseGroup ->
                                                                Group(
                                                                        id =
                                                                                firebaseGroup.id
                                                                                        .hashCode(),
                                                                        name = firebaseGroup.name,
                                                                        details =
                                                                                "${firebaseGroup.members.size} members • ${firebaseGroup.subject}",
                                                                        assignmentCount =
                                                                                0, // you can
                                                                        // calculate real
                                                                        // assignments later
                                                                        iconColor = "#007AFF",
                                                                        iconResource =
                                                                                R.drawable.ic_groups
                                                                )
                                                        }

                                                myGroupsAdapter = GroupAdapter { group ->
                                                        val intent =
                                                                android.content.Intent(
                                                                        context,
                                                                        GroupDetailsActivity::class
                                                                                .java
                                                                )
                                                        intent.putExtra(
                                                                "GROUP_ID",
                                                                firebaseGroups
                                                                        .find {
                                                                                it.name ==
                                                                                        group.name
                                                                        }
                                                                        ?.id
                                                                        ?: ""
                                                        )
                                                        startActivity(intent)
                                                }
                                                myGroupsAdapter.submitList(displayGroups)
                                                _binding?.recyclerMyGroups?.adapter =
                                                        myGroupsAdapter

                                                // Hide refresh indicator when data loads
                                                _binding?.swipeRefreshLayout?.isRefreshing = false
                                        },
                                        onFailure = { exception ->
                                                Log.e(TAG, "Error loading groups", exception)
                                                context?.let { ctx ->
                                                        ErrorHandler.handleError(
                                                                ctx,
                                                                exception,
                                                                _binding?.root
                                                        ) {
                                                                // Retry callback
                                                                loadGroupsData()
                                                        }
                                                }
                                                _binding?.swipeRefreshLayout?.isRefreshing = false
                                        }
                                )
                        } catch (e: Exception) {
                                Log.e(TAG, "Error refreshing groups", e)
                                context?.let { ctx ->
                                        ErrorHandler.handleError(ctx, e, _binding?.root) {
                                                // Retry callback
                                                loadGroupsData()
                                        }
                                }
                                _binding?.swipeRefreshLayout?.isRefreshing = false
                        }
                }
        }

        private fun updateMyGroupsEmptyState(isEmpty: Boolean) {
                val binding =
                        _binding
                                ?: run {
                                        Log.d(
                                                TAG,
                                                "Cannot update my groups empty state: view is destroyed"
                                        )
                                        return
                                }

                if (isEmpty) {
                        binding.emptyStateMyGroups.visibility = View.VISIBLE
                        binding.recyclerMyGroups.visibility = View.GONE
                        Log.d(TAG, "Showing empty state for my groups")
                } else {
                        binding.emptyStateMyGroups.visibility = View.GONE
                        binding.recyclerMyGroups.visibility = View.VISIBLE
                        Log.d(TAG, "Hiding empty state for my groups")
                }
        }

        private fun updateActivityEmptyState(isEmpty: Boolean) {
                val binding =
                        _binding
                                ?: run {
                                        Log.d(
                                                TAG,
                                                "Cannot update activity empty state: view is destroyed"
                                        )
                                        return
                                }

                if (isEmpty) {
                        binding.emptyStateActivity.visibility = View.VISIBLE
                        binding.recyclerRecentActivity.visibility = View.GONE
                } else {
                        binding.emptyStateActivity.visibility = View.GONE
                        binding.recyclerRecentActivity.visibility = View.VISIBLE
                }
        }

        private fun updateDiscoverGroupsEmptyState(isEmpty: Boolean) {
                val binding =
                        _binding
                                ?: run {
                                        Log.d(
                                                TAG,
                                                "Cannot update discover groups empty state: view is destroyed"
                                        )
                                        return
                                }

                if (isEmpty) {
                        binding.emptyStateDiscover.visibility = View.VISIBLE
                        binding.recyclerDiscoverGroups.visibility = View.GONE
                } else {
                        binding.emptyStateDiscover.visibility = View.GONE
                        binding.recyclerDiscoverGroups.visibility = View.VISIBLE
                }
        }

        /**
         * Handle errors that occur when loading groups data Maps exceptions to user-friendly error
         * messages
         */
        private fun handleGroupsError(exception: Exception) {
                Log.e(TAG, "Handling groups error", exception)

                // Use ErrorHandler for consistent error handling across the app
                if (_binding != null && isAdded) {
                        context?.let { ctx ->
                                ErrorHandler.handleError(ctx, exception, _binding?.root) {
                                        // Retry callback - refresh the data
                                        refreshData()
                                }
                        }

                        // Show empty state for groups
                        updateMyGroupsEmptyState(true)
                } else {
                        Log.d(TAG, "View destroyed, skipping error UI update")
                }
        }

        private fun getDummyGroups(): List<Group> {
                return listOf(
                        Group(
                                id = 1,
                                name = "Computer Science Team",
                                details =
                                        "5 members • 3 active assignments • Last active 2 hours ago",
                                assignmentCount = 3,
                                iconColor = "#007AFF",
                                iconResource = R.drawable.ic_groups
                        ),
                        Group(
                                id = 2,
                                name = "Study Group Alpha",
                                details = "8 members • 1 active assignment • Last active 1 day ago",
                                assignmentCount = 1,
                                iconColor = "#34C759",
                                iconResource = R.drawable.ic_groups
                        ),
                        Group(
                                id = 3,
                                name = "Math Study Group",
                                details =
                                        "6 members • 2 active assignments • Last active 30 min ago",
                                assignmentCount = 2,
                                iconColor = "#FF9500",
                                iconResource = R.drawable.ic_assignment
                        ),
                        Group(
                                id = 4,
                                name = "Literature Circle",
                                details =
                                        "4 members • 0 active assignments • Last active 3 days ago",
                                assignmentCount = 0,
                                iconColor = "#AF52DE",
                                iconResource = R.drawable.ic_assignment
                        ),
                        Group(
                                id = 5,
                                name = "Chemistry Lab Group",
                                details =
                                        "7 members • 4 active assignments • Last active 1 hour ago",
                                assignmentCount = 4,
                                iconColor = "#FF3B30",
                                iconResource = R.drawable.ic_assignment
                        )
                )
        }

        private fun getDummyActivities(): List<Activity> {
                return listOf(
                        Activity(
                                id = 1,
                                title = "New message in Computer Science Team",
                                details =
                                        "Sarah: \"Has anyone started the research paper?\" • 2 hours ago",
                                iconColor = "#007AFF",
                                iconResource = R.drawable.ic_chat
                        ),
                        Activity(
                                id = 2,
                                title = "New assignment in Math Study Group",
                                details = "Calculus Problem Set #5 • Due in 3 days",
                                iconColor = "#34C759",
                                iconResource = R.drawable.ic_assignment
                        ),
                        Activity(
                                id = 3,
                                title = "New member joined Chemistry Lab Group",
                                details = "Mike Johnson joined the group • 1 day ago",
                                iconColor = "#FF9500",
                                iconResource = R.drawable.ic_person
                        )
                )
        }

        private fun getDummyDiscoverGroups(): List<DiscoverGroup> {
                return listOf(
                        DiscoverGroup(
                                id = 6,
                                name = "Art & Design Club",
                                details = "15 members • Open to join • Creative projects",
                                iconColor = "#5856D6",
                                iconResource = R.drawable.ic_palette
                        ),
                        DiscoverGroup(
                                id = 7,
                                name = "Music Theory Study",
                                details = "12 members • Open to join • Music theory practice",
                                iconColor = "#FF2D92",
                                iconResource = R.drawable.ic_assignment
                        )
                )
        }

        override fun onStart() {
                super.onStart()
                Log.d(TAG, "onStart - Firestore listeners will be attached")
        }

        override fun onStop() {
                super.onStop()
                Log.d(TAG, "onStop - Firestore listeners will be detached")
        }
}
