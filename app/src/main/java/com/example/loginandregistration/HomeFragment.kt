package com.example.loginandregistration

import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.MenuProvider
import androidx.lifecycle.Lifecycle
import com.example.loginandregistration.databinding.FragmentHomeBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = Firebase.auth
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupToolbar()
        setupUI()
        setupClickListeners()
    }

    private fun setupToolbar() {
        // Setup Toolbar
        (activity as? AppCompatActivity)?.setSupportActionBar(binding.toolbarHome)
        (activity as? AppCompatActivity)?.supportActionBar?.title = getString(R.string.dashboard_title)

        // Setup menu using the new MenuProvider API
        requireActivity().addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.home_toolbar_menu, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return when (menuItem.itemId) {
                    R.id.action_notifications -> {
                        Toast.makeText(context, R.string.navigating_to_notifications, Toast.LENGTH_SHORT).show()
                        // TODO: Implement navigation to Notifications screen/fragment
                        true
                    }
                    R.id.action_profile -> {
                        (activity as? MainActivity)?.navigateToProfile()
                        true
                    }
                    else -> false
                }
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }

    private fun setupUI() {
        // Update Welcome Message
        val user = auth.currentUser
        val welcomeName = user?.displayName?.takeIf { it.isNotBlank() } ?: user?.email ?: "User"
        binding.tvWelcomeTitle.text = getString(R.string.welcome_message, welcomeName).replace("👋 ", "").replace(",","") // Basic formatting

        // Set static data for now - replace with dynamic data later
        binding.tvTasksDueCount.text = "5"
        binding.tvGroupsCount.text = "3"
        binding.tvSessionsCount.text = "2"  // Fixed: was using : instead of .
        binding.tvAiPromptsLeft.text = getString(R.string.home_ai_prompts_left_template, 7)
        binding.progressBarAiUsage.progress = 30
        binding.tvAiUsageDetails.text = getString(R.string.home_ai_prompts_usage_template, 3, 10)
    }

    private fun setupClickListeners() {
        binding.btnViewAllTasksHome.setOnClickListener {
            Toast.makeText(context, R.string.button_view_all_tasks_clicked, Toast.LENGTH_SHORT).show()
            (activity as? MainActivity)?.navigateToTasksScreen()
        }

        binding.btnViewAllGroupsHome.setOnClickListener {
            Toast.makeText(context, R.string.button_view_all_groups_clicked, Toast.LENGTH_SHORT).show()
            (activity as? MainActivity)?.navigateToGroupsScreen()
        }

        binding.btnViewAllScheduleHome.setOnClickListener {
            Toast.makeText(context, R.string.button_view_all_schedule_clicked, Toast.LENGTH_SHORT).show()
            (activity as? MainActivity)?.navigateToCalendarScreen()
        }

        binding.btnNewTaskHome.setOnClickListener {
            Toast.makeText(context, R.string.new_task_clicked, Toast.LENGTH_SHORT).show()
            // TODO: Implement navigation or dialog for new task
        }

        binding.btnCalendarQuickHome.setOnClickListener {
            Toast.makeText(context, R.string.calendar_quick_action_clicked, Toast.LENGTH_SHORT).show()
            (activity as? MainActivity)?.navigateToCalendarScreen()
        }

        binding.btnGroupsQuickHome.setOnClickListener {
            Toast.makeText(context, R.string.groups_quick_action_clicked, Toast.LENGTH_SHORT).show()
            (activity as? MainActivity)?.navigateToGroupsScreen()
        }

        binding.btnAiAssistantHome.setOnClickListener {
            Toast.makeText(context, R.string.ai_assistant_clicked, Toast.LENGTH_SHORT).show()
            // TODO: Implement navigation to AI Assistant
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}