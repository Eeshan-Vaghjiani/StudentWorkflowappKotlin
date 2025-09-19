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

class TasksFragment : Fragment() {

    private lateinit var recyclerTasks: RecyclerView
    private lateinit var taskAdapter: TaskAdapter

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_tasks, container, false)

        // Show a toast to confirm the fragment is loading
        Toast.makeText(context, "TasksFragment loaded successfully!", Toast.LENGTH_LONG).show()

        setupViews(view)
        setupRecyclerView(view)
        setupClickListeners(view)

        return view
    }

    private fun setupViews(view: View) {
        recyclerTasks = view.findViewById(R.id.recycler_tasks)
    }

    private fun setupRecyclerView(view: View) {
        val tasks = getDummyTasks()
        taskAdapter =
                TaskAdapter(tasks) { task ->
                    Toast.makeText(
                                    context,
                                    getString(R.string.task_clicked, task.title),
                                    Toast.LENGTH_SHORT
                            )
                            .show()
                }

        recyclerTasks.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = taskAdapter
        }
    }

    private fun setupClickListeners(view: View) {
        // Header buttons
        view.findViewById<ImageButton>(R.id.btn_filter)?.setOnClickListener {
            Toast.makeText(context, getString(R.string.filter_clicked), Toast.LENGTH_SHORT).show()
        }

        view.findViewById<ImageButton>(R.id.btn_search)?.setOnClickListener {
            Toast.makeText(context, getString(R.string.search_clicked), Toast.LENGTH_SHORT).show()
        }

        view.findViewById<ImageButton>(R.id.btn_add_task)?.setOnClickListener {
            Toast.makeText(context, getString(R.string.add_task_clicked), Toast.LENGTH_SHORT).show()
        }

        // Category buttons
        view.findViewById<MaterialButton>(R.id.btn_all_tasks)?.setOnClickListener {
            Toast.makeText(
                            context,
                            getString(R.string.category_selected, "All Tasks"),
                            Toast.LENGTH_SHORT
                    )
                    .show()
        }

        view.findViewById<MaterialButton>(R.id.btn_personal)?.setOnClickListener {
            Toast.makeText(
                            context,
                            getString(R.string.category_selected, "Personal"),
                            Toast.LENGTH_SHORT
                    )
                    .show()
        }

        view.findViewById<MaterialButton>(R.id.btn_group)?.setOnClickListener {
            Toast.makeText(
                            context,
                            getString(R.string.category_selected, "Group"),
                            Toast.LENGTH_SHORT
                    )
                    .show()
        }

        view.findViewById<MaterialButton>(R.id.btn_assignments)?.setOnClickListener {
            Toast.makeText(
                            context,
                            getString(R.string.category_selected, "Assignments"),
                            Toast.LENGTH_SHORT
                    )
                    .show()
        }

        // View toggle buttons
        view.findViewById<ImageButton>(R.id.btn_list_view)?.setOnClickListener {
            Toast.makeText(context, getString(R.string.view_changed, "List"), Toast.LENGTH_SHORT)
                    .show()
        }

        view.findViewById<ImageButton>(R.id.btn_kanban_view)?.setOnClickListener {
            Toast.makeText(context, getString(R.string.view_changed, "Kanban"), Toast.LENGTH_SHORT)
                    .show()
        }

        // Quick action buttons
        view.findViewById<MaterialButton>(R.id.btn_new_task)?.setOnClickListener {
            Toast.makeText(context, getString(R.string.add_task_clicked), Toast.LENGTH_SHORT).show()
        }

        view.findViewById<MaterialButton>(R.id.btn_kanban_view_action)?.setOnClickListener {
            Toast.makeText(context, getString(R.string.view_changed, "Kanban"), Toast.LENGTH_SHORT)
                    .show()
        }

        view.findViewById<MaterialButton>(R.id.btn_ai_assistant)?.setOnClickListener {
            Toast.makeText(context, getString(R.string.ai_assistant_clicked), Toast.LENGTH_SHORT)
                    .show()
        }

        view.findViewById<MaterialButton>(R.id.btn_export)?.setOnClickListener {
            Toast.makeText(context, "Export clicked", Toast.LENGTH_SHORT).show()
        }
    }

    private fun getDummyTasks(): List<Task> {
        return listOf(
                Task(
                        id = 1,
                        title = "Research Paper Draft",
                        subtitle = "Overdue • Computer Science • Due 2 days ago",
                        status = "Overdue",
                        iconColor = "#FF3B30",
                        statusColor = "#FF3B30"
                ),
                Task(
                        id = 2,
                        title = "Math Assignment",
                        subtitle = "Due today • Mathematics • Due in 4 hours",
                        status = "Due Today",
                        iconColor = "#FF9500",
                        statusColor = "#FF9500"
                ),
                Task(
                        id = 3,
                        title = "Lab Report",
                        subtitle = "Due tomorrow • Chemistry • Due in 1 day",
                        status = "Due Tomorrow",
                        iconColor = "#007AFF",
                        statusColor = "#007AFF"
                ),
                Task(
                        id = 4,
                        title = "Literature Review",
                        subtitle = "Due in 3 days • English • Due in 3 days",
                        status = "Due Later",
                        iconColor = "#34C759",
                        statusColor = "#34C759"
                ),
                Task(
                        id = 5,
                        title = "Physics Quiz",
                        subtitle = "Completed • Physics • Completed yesterday",
                        status = "Completed",
                        iconColor = "#8E8E93",
                        statusColor = "#8E8E93"
                )
        )
    }
}
