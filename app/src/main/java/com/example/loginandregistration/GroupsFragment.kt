package com.example.loginandregistration

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.loginandregistration.models.*
import com.example.loginandregistration.repository.EnhancedGroupRepository
import com.example.loginandregistration.repository.GroupRepository
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.launch

class GroupsFragment : Fragment() {

        private lateinit var recyclerMyGroups: RecyclerView
        private lateinit var recyclerRecentActivity: RecyclerView
        private lateinit var recyclerDiscoverGroups: RecyclerView

        private lateinit var myGroupsAdapter: GroupAdapter
        private lateinit var activityAdapter: ActivityAdapter
        private lateinit var discoverGroupsAdapter: DiscoverGroupAdapter

        private lateinit var groupRepository: GroupRepository

        override fun onCreateView(
                inflater: LayoutInflater,
                container: ViewGroup?,
                savedInstanceState: Bundle?
        ): View? {
                val view = inflater.inflate(R.layout.fragment_groups, container, false)

                groupRepository = GroupRepository()
                setupViews(view)
                setupRecyclerViews(view)
                setupClickListeners(view)
                loadGroupsData()

                return view
        }

        private fun setupViews(view: View) {
                recyclerMyGroups = view.findViewById(R.id.recycler_my_groups)
                recyclerRecentActivity = view.findViewById(R.id.recycler_recent_activity)
                recyclerDiscoverGroups = view.findViewById(R.id.recycler_discover_groups)
        }

        private fun setupRecyclerViews(view: View) {
                // My Groups
                val myGroups = getDummyGroups()
                myGroupsAdapter =
                        GroupAdapter(myGroups) { group ->
                                Toast.makeText(
                                                context,
                                                getString(R.string.group_clicked, group.name),
                                                Toast.LENGTH_SHORT
                                        )
                                        .show()
                        }

                recyclerMyGroups.apply {
                        layoutManager = LinearLayoutManager(context)
                        adapter = myGroupsAdapter
                }

                // Recent Activity
                val activities = getDummyActivities()
                activityAdapter =
                        ActivityAdapter(activities) { _ ->
                                Toast.makeText(
                                                context,
                                                getString(R.string.activity_clicked),
                                                Toast.LENGTH_SHORT
                                        )
                                        .show()
                        }

                recyclerRecentActivity.apply {
                        layoutManager = LinearLayoutManager(context)
                        adapter = activityAdapter
                }

                // Discover Groups
                val discoverGroups = getDummyDiscoverGroups()
                discoverGroupsAdapter =
                        DiscoverGroupAdapter(discoverGroups) { group -> joinDiscoverGroup(group) }

                recyclerDiscoverGroups.apply {
                        layoutManager = LinearLayoutManager(context)
                        adapter = discoverGroupsAdapter
                }
        }

        private fun setupClickListeners(view: View) {
                // Header buttons
                view.findViewById<ImageButton>(R.id.btn_search_groups)?.setOnClickListener {
                        Toast.makeText(
                                        context,
                                        getString(R.string.search_clicked),
                                        Toast.LENGTH_SHORT
                                )
                                .show()
                }

                view.findViewById<ImageButton>(R.id.btn_join_group)?.setOnClickListener {
                        showJoinGroupDialog()
                }

                // Quick action buttons
                view.findViewById<MaterialButton>(R.id.btn_create_group)?.setOnClickListener {
                        showCreateGroupDialog()
                }

                view.findViewById<MaterialButton>(R.id.btn_join_group_action)?.setOnClickListener {
                        showJoinGroupDialog()
                }

                view.findViewById<MaterialButton>(R.id.btn_assignments)?.setOnClickListener {
                        Toast.makeText(context, "Assignments clicked", Toast.LENGTH_SHORT).show()
                }

                view.findViewById<MaterialButton>(R.id.btn_group_chat)?.setOnClickListener {
                        Toast.makeText(context, "Group chat clicked", Toast.LENGTH_SHORT).show()
                }

                // Navigation buttons
                view.findViewById<ImageButton>(R.id.btn_show_all_groups)?.setOnClickListener {
                        Toast.makeText(context, "Show all groups clicked", Toast.LENGTH_SHORT)
                                .show()
                }

                view.findViewById<ImageButton>(R.id.btn_show_more_groups)?.setOnClickListener {
                        Toast.makeText(context, "Show more groups clicked", Toast.LENGTH_SHORT)
                                .show()
                }
        }

        private fun loadGroupsData() {
                lifecycleScope.launch {
                        try {
                                // Load user's groups using enhanced repository
                                val enhancedRepository =
                                        com.example.loginandregistration.repository
                                                .EnhancedGroupRepository()

                                // Collect real-time updates
                                enhancedRepository.getUserGroups().collect { userGroups ->
                                        val displayGroups =
                                                userGroups.map { firebaseGroup ->
                                                        Group(
                                                                id = firebaseGroup.id.hashCode(),
                                                                name = firebaseGroup.name,
                                                                details =
                                                                        "${firebaseGroup.members.size} members • ${firebaseGroup.subject}",
                                                                assignmentCount =
                                                                        0, // TODO: Implement when
                                                                // tasks are
                                                                // linked to groups
                                                                iconColor = "#007AFF",
                                                                iconResource = R.drawable.ic_groups
                                                        )
                                                }

                                        myGroupsAdapter =
                                                GroupAdapter(displayGroups) { group ->
                                                        // Navigate to group details
                                                        val intent =
                                                                android.content.Intent(
                                                                        context,
                                                                        GroupDetailsActivity::class
                                                                                .java
                                                                )
                                                        intent.putExtra(
                                                                "GROUP_ID",
                                                                userGroups
                                                                        .find {
                                                                                it.name ==
                                                                                        group.name
                                                                        }
                                                                        ?.id
                                                                        ?: ""
                                                        )
                                                        startActivity(intent)
                                                }
                                        recyclerMyGroups.adapter = myGroupsAdapter

                                        // Load recent activities
                                        val activities = groupRepository.getGroupActivities()
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

                                        activityAdapter =
                                                ActivityAdapter(displayActivities) { _ ->
                                                        Toast.makeText(
                                                                        context,
                                                                        getString(
                                                                                R.string
                                                                                        .activity_clicked
                                                                        ),
                                                                        Toast.LENGTH_SHORT
                                                                )
                                                                .show()
                                                }
                                        recyclerRecentActivity.adapter = activityAdapter

                                        // Load discoverable groups
                                        val publicGroups = groupRepository.getPublicGroups()
                                        val displayDiscoverGroups =
                                                publicGroups.map { firebaseGroup ->
                                                        DiscoverGroup(
                                                                id = firebaseGroup.id.hashCode(),
                                                                name = firebaseGroup.name,
                                                                details =
                                                                        "${firebaseGroup.members.size} members • ${firebaseGroup.subject}",
                                                                iconColor = "#5856D6",
                                                                iconResource = R.drawable.ic_groups
                                                        )
                                                }

                                        discoverGroupsAdapter =
                                                DiscoverGroupAdapter(displayDiscoverGroups) { group
                                                        ->
                                                        joinDiscoverGroup(group)
                                                }
                                        recyclerDiscoverGroups.adapter = discoverGroupsAdapter

                                        // Load and update statistics
                                        val groupStats = groupRepository.getGroupStats()
                                        view?.findViewById<android.widget.TextView>(
                                                        R.id.tv_my_groups_count
                                                )
                                                ?.text = groupStats.myGroups.toString()
                                        view?.findViewById<android.widget.TextView>(
                                                        R.id.tv_active_assignments_count
                                                )
                                                ?.text = groupStats.activeAssignments.toString()
                                        view?.findViewById<android.widget.TextView>(
                                                        R.id.tv_new_messages_count
                                                )
                                                ?.text = groupStats.newMessages.toString()
                                }
                        } catch (e: Exception) {
                                Toast.makeText(context, "Error loading groups", Toast.LENGTH_SHORT)
                                        .show()
                                // Keep dummy data as fallback
                        }
                }
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
                                val groupId =
                                        groupRepository.createGroup(
                                                name,
                                                description,
                                                subject,
                                                "public"
                                        )
                                if (groupId != null) {
                                        Toast.makeText(
                                                        context,
                                                        getString(
                                                                R.string.group_created_successfully
                                                        ),
                                                        Toast.LENGTH_SHORT
                                                )
                                                .show()
                                        dialog.dismiss()
                                        loadGroupsData() // Refresh the data
                                } else {
                                        Toast.makeText(
                                                        context,
                                                        getString(R.string.error_creating_group),
                                                        Toast.LENGTH_SHORT
                                                )
                                                .show()
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
                                val success = groupRepository.joinGroupByCode(code)
                                if (success) {
                                        Toast.makeText(
                                                        context,
                                                        getString(
                                                                R.string.joined_group_successfully
                                                        ),
                                                        Toast.LENGTH_SHORT
                                                )
                                                .show()
                                        dialog.dismiss()
                                        loadGroupsData() // Refresh the data
                                } else {
                                        Toast.makeText(
                                                        context,
                                                        getString(R.string.error_joining_group),
                                                        Toast.LENGTH_SHORT
                                                )
                                                .show()
                                }
                        }
                }

                dialog.show()
        }

        private fun joinDiscoverGroup(group: DiscoverGroup) {
                lifecycleScope.launch {
                        // Find the actual Firebase group by name (this is a simplified approach)
                        val publicGroups = groupRepository.getPublicGroups()
                        val firebaseGroup = publicGroups.find { it.name == group.name }

                        if (firebaseGroup != null) {
                                val success = groupRepository.joinGroup(firebaseGroup.id)
                                if (success) {
                                        Toast.makeText(
                                                        context,
                                                        getString(
                                                                R.string.discover_group_joined,
                                                                group.name
                                                        ),
                                                        Toast.LENGTH_SHORT
                                                )
                                                .show()
                                        loadGroupsData() // Refresh the data
                                } else {
                                        Toast.makeText(
                                                        context,
                                                        getString(R.string.error_joining_group),
                                                        Toast.LENGTH_SHORT
                                                )
                                                .show()
                                }
                        }
                }
        }

        private fun getActivityIcon(type: String): Int {
                return when (type) {
                        "message" -> R.drawable.ic_chat
                        "assignment" -> R.drawable.ic_assignment
                        "member_joined" -> R.drawable.ic_person
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
                        else -> "${diff / (24 * 60 * 60 * 1000)} days ago"
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
}
