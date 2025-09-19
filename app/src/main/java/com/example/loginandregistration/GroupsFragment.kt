package com.example.loginandregistration

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton

class GroupsFragment : Fragment() {

        private lateinit var recyclerMyGroups: RecyclerView
        private lateinit var recyclerRecentActivity: RecyclerView
        private lateinit var recyclerDiscoverGroups: RecyclerView

        private lateinit var myGroupsAdapter: GroupAdapter
        private lateinit var activityAdapter: ActivityAdapter
        private lateinit var discoverGroupsAdapter: DiscoverGroupAdapter

        override fun onCreateView(
                inflater: LayoutInflater,
                container: ViewGroup?,
                savedInstanceState: Bundle?
        ): View? {
                val view = inflater.inflate(R.layout.fragment_groups, container, false)

                // Show a toast to confirm the fragment is loading
                Toast.makeText(context, "GroupsFragment loaded successfully!", Toast.LENGTH_LONG)
                        .show()

                setupViews(view)
                setupRecyclerViews(view)
                setupClickListeners(view)

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
                        ActivityAdapter(activities) { activity ->
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
                        DiscoverGroupAdapter(discoverGroups) { group ->
                                Toast.makeText(
                                                context,
                                                getString(
                                                        R.string.discover_group_joined,
                                                        group.name
                                                ),
                                                Toast.LENGTH_SHORT
                                        )
                                        .show()
                        }

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
                        Toast.makeText(
                                        context,
                                        getString(R.string.join_group_clicked),
                                        Toast.LENGTH_SHORT
                                )
                                .show()
                }

                // Quick action buttons
                view.findViewById<MaterialButton>(R.id.btn_create_group)?.setOnClickListener {
                        Toast.makeText(
                                        context,
                                        getString(R.string.create_group_clicked),
                                        Toast.LENGTH_SHORT
                                )
                                .show()
                }

                view.findViewById<MaterialButton>(R.id.btn_join_group_action)?.setOnClickListener {
                        Toast.makeText(
                                        context,
                                        getString(R.string.join_group_clicked),
                                        Toast.LENGTH_SHORT
                                )
                                .show()
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
