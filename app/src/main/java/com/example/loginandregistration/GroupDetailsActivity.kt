package com.example.loginandregistration

import android.app.AlertDialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.loginandregistration.databinding.ActivityGroupDetailsBinding
import com.example.loginandregistration.models.GroupMember
import com.example.loginandregistration.repository.GroupRepository
import com.example.loginandregistration.repository.UserRepository
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.launch

class GroupDetailsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityGroupDetailsBinding
    private lateinit var groupRepository: GroupRepository
    private lateinit var userRepository: UserRepository
    private lateinit var membersAdapter: MembersAdapter

    private var groupId: String = ""
    private var isUserAdmin = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGroupDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        groupRepository = GroupRepository()
        userRepository = UserRepository()

        groupId = intent.getStringExtra("GROUP_ID") ?: ""
        if (groupId.isEmpty()) {
            finish()
            return
        }

        setupToolbar()
        setupRecyclerView()
        setupClickListeners()
        loadGroupDetails()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Group Details"

        binding.toolbar.setNavigationOnClickListener { finish() }
    }

    private fun setupRecyclerView() {
        membersAdapter =
                MembersAdapter(emptyList()) { member ->
                    if (isUserAdmin) {
                        showMemberOptionsDialog(member)
                    }
                }

        binding.recyclerMembers.apply {
            layoutManager = LinearLayoutManager(this@GroupDetailsActivity)
            adapter = membersAdapter
        }
    }

    private fun setupClickListeners() {
        binding.btnCopyJoinCode.setOnClickListener { copyJoinCodeToClipboard() }

        binding.btnAddMember.setOnClickListener {
            if (isUserAdmin) {
                showAddMemberDialog()
            } else {
                Toast.makeText(this, "Only admins can add members", Toast.LENGTH_SHORT).show()
            }
        }

        binding.btnRegenerateCode.setOnClickListener {
            if (isUserAdmin) {
                regenerateJoinCode()
            } else {
                Toast.makeText(this, "Only admins can regenerate join code", Toast.LENGTH_SHORT)
                        .show()
            }
        }

        binding.btnEditGroup.setOnClickListener {
            if (isUserAdmin) {
                showEditGroupDialog()
            } else {
                Toast.makeText(this, "Only admins can edit group details", Toast.LENGTH_SHORT)
                        .show()
            }
        }

        binding.btnDeleteGroup?.setOnClickListener {
            if (isUserAdmin) {
                showDeleteGroupDialog()
            } else {
                Toast.makeText(this, "Only owner can delete group", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun loadGroupDetails() {
        lifecycleScope.launch {
            try {
                val group = groupRepository.getGroupById(groupId)
                if (group != null) {
                    binding.tvGroupName.text = group.name
                    binding.tvGroupDescription.text = group.description
                    binding.tvGroupSubject.text = "Subject: ${group.subject}"

                    // Check if current user is admin or owner
                    val currentUserId =
                            com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid
                    isUserAdmin =
                            currentUserId != null &&
                                    (group.owner == currentUserId ||
                                            group.members.any {
                                                it.userId == currentUserId &&
                                                        it.role in listOf("owner", "admin")
                                            })

                    // Show/hide admin controls
                    binding.cardJoinCode.visibility =
                            if (isUserAdmin) android.view.View.VISIBLE else android.view.View.GONE
                    binding.btnAddMember.visibility =
                            if (isUserAdmin) android.view.View.VISIBLE else android.view.View.GONE

                    if (isUserAdmin) {
                        binding.tvJoinCode.text = group.joinCode
                    }

                    // Load members
                    loadMembers(group.members)
                } else {
                    Toast.makeText(this@GroupDetailsActivity, "Group not found", Toast.LENGTH_SHORT)
                            .show()
                    finish()
                }
            } catch (e: Exception) {
                Toast.makeText(
                                this@GroupDetailsActivity,
                                "Error loading group details",
                                Toast.LENGTH_SHORT
                        )
                        .show()
                finish()
            }
        }
    }

    private fun loadMembers(members: List<GroupMember>) {
        lifecycleScope.launch {
            try {
                val memberList =
                        members.map { member ->
                            // Fetch user profile image from users collection
                            val firebaseUser = userRepository.getUserById(member.userId)
                            val profileImageUrl = firebaseUser?.photoUrl ?: ""

                            Member(
                                    userId = member.userId,
                                    name = member.displayName,
                                    email = member.email,
                                    isAdmin = member.role in listOf("owner", "admin"),
                                    isOnline = member.isActive,
                                    profileImageUrl = profileImageUrl
                            )
                        }

                membersAdapter =
                        MembersAdapter(memberList) { member ->
                            if (isUserAdmin) {
                                showMemberOptionsDialog(member)
                            }
                        }
                binding.recyclerMembers.adapter = membersAdapter
            } catch (e: Exception) {
                Toast.makeText(
                                this@GroupDetailsActivity,
                                "Error loading members",
                                Toast.LENGTH_SHORT
                        )
                        .show()
            }
        }
    }

    private fun copyJoinCodeToClipboard() {
        val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("Group Join Code", binding.tvJoinCode.text)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(this, "Join code copied to clipboard", Toast.LENGTH_SHORT).show()
    }

    private fun showAddMemberDialog() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_member, null)
        val dialog = AlertDialog.Builder(this).setView(dialogView).create()

        val etSearchUser = dialogView.findViewById<TextInputEditText>(R.id.et_search_user)
        val btnCancel = dialogView.findViewById<MaterialButton>(R.id.btn_cancel)
        val btnAdd = dialogView.findViewById<MaterialButton>(R.id.btn_add_member)

        btnCancel.setOnClickListener { dialog.dismiss() }

        btnAdd.setOnClickListener {
            val searchQuery = etSearchUser.text.toString().trim()
            if (searchQuery.isEmpty()) {
                Toast.makeText(this, "Please enter email or name", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            lifecycleScope.launch {
                try {
                    val users = userRepository.searchUsers(searchQuery)
                    if (users.isNotEmpty()) {
                        val user = users.first()
                        val success = groupRepository.addMemberToGroup(groupId, user.uid)
                        if (success) {
                            Toast.makeText(
                                            this@GroupDetailsActivity,
                                            "Member added successfully",
                                            Toast.LENGTH_SHORT
                                    )
                                    .show()
                            dialog.dismiss()
                            loadGroupDetails() // Refresh
                        } else {
                            Toast.makeText(
                                            this@GroupDetailsActivity,
                                            "Failed to add member",
                                            Toast.LENGTH_SHORT
                                    )
                                    .show()
                        }
                    } else {
                        Toast.makeText(
                                        this@GroupDetailsActivity,
                                        "User not found",
                                        Toast.LENGTH_SHORT
                                )
                                .show()
                    }
                } catch (e: Exception) {
                    Toast.makeText(
                                    this@GroupDetailsActivity,
                                    "Error searching user",
                                    Toast.LENGTH_SHORT
                            )
                            .show()
                }
            }
        }

        dialog.show()
    }

    private fun showMemberOptionsDialog(member: Member) {
        val options =
                if (member.isAdmin) {
                    arrayOf("Remove Admin", "Remove from Group")
                } else {
                    arrayOf("Make Admin", "Remove from Group")
                }

        AlertDialog.Builder(this)
                .setTitle("Member Options")
                .setItems(options) { _, which ->
                    when (which) {
                        0 -> {
                            if (member.isAdmin) {
                                // Remove admin (implement if needed)
                                Toast.makeText(
                                                this,
                                                "Remove admin functionality not implemented",
                                                Toast.LENGTH_SHORT
                                        )
                                        .show()
                            } else {
                                // Make admin (implement if needed)
                                Toast.makeText(
                                                this,
                                                "Make admin functionality not implemented",
                                                Toast.LENGTH_SHORT
                                        )
                                        .show()
                            }
                        }
                        1 -> {
                            // Remove from group
                            removeMemberFromGroup(member)
                        }
                    }
                }
                .show()
    }

    private fun removeMemberFromGroup(member: Member) {
        lifecycleScope.launch {
            try {
                val success = groupRepository.removeMemberFromGroup(groupId, member.userId)
                if (success) {
                    Toast.makeText(
                                    this@GroupDetailsActivity,
                                    "Member removed successfully",
                                    Toast.LENGTH_SHORT
                            )
                            .show()
                    loadGroupDetails() // Refresh
                } else {
                    Toast.makeText(
                                    this@GroupDetailsActivity,
                                    "Failed to remove member",
                                    Toast.LENGTH_SHORT
                            )
                            .show()
                }
            } catch (e: Exception) {
                Toast.makeText(
                                this@GroupDetailsActivity,
                                "Error removing member",
                                Toast.LENGTH_SHORT
                        )
                        .show()
            }
        }
    }

    private fun regenerateJoinCode() {
        lifecycleScope.launch {
            try {
                val newCode = groupRepository.regenerateJoinCode(groupId)
                if (newCode != null) {
                    binding.tvJoinCode.text = newCode
                    Toast.makeText(
                                    this@GroupDetailsActivity,
                                    "Join code regenerated",
                                    Toast.LENGTH_SHORT
                            )
                            .show()
                } else {
                    Toast.makeText(
                                    this@GroupDetailsActivity,
                                    "Failed to regenerate join code",
                                    Toast.LENGTH_SHORT
                            )
                            .show()
                }
            } catch (e: Exception) {
                Toast.makeText(
                                this@GroupDetailsActivity,
                                "Error regenerating join code",
                                Toast.LENGTH_SHORT
                        )
                        .show()
            }
        }
    }

    private fun showEditGroupDialog() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_create_group, null)
        val dialog = AlertDialog.Builder(this).setView(dialogView).create()

        val etGroupName = dialogView.findViewById<TextInputEditText>(R.id.et_group_name)
        val etGroupDescription =
                dialogView.findViewById<TextInputEditText>(R.id.et_group_description)
        val etGroupSubject = dialogView.findViewById<TextInputEditText>(R.id.et_group_subject)
        val btnCancel = dialogView.findViewById<MaterialButton>(R.id.btn_cancel)
        val btnCreate = dialogView.findViewById<MaterialButton>(R.id.btn_create)

        // Pre-fill with current values
        etGroupName.setText(binding.tvGroupName.text)
        etGroupDescription.setText(binding.tvGroupDescription.text)
        etGroupSubject.setText(binding.tvGroupSubject.text.toString().replace("Subject: ", ""))

        // Change button text to "Update"
        btnCreate.text = "Update"

        btnCancel.setOnClickListener { dialog.dismiss() }

        btnCreate.setOnClickListener {
            val name = etGroupName.text.toString().trim()
            val description = etGroupDescription.text.toString().trim()
            val subject = etGroupSubject.text.toString().trim()

            if (name.isEmpty()) {
                Toast.makeText(this, "Group name is required", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            lifecycleScope.launch {
                val success =
                        groupRepository.updateGroupDetails(
                                groupId,
                                name,
                                description,
                                subject,
                                "public" // TODO: Add privacy selection
                        )
                if (success) {
                    Toast.makeText(
                                    this@GroupDetailsActivity,
                                    "Group updated successfully",
                                    Toast.LENGTH_SHORT
                            )
                            .show()
                    dialog.dismiss()
                    loadGroupDetails() // Refresh
                } else {
                    Toast.makeText(
                                    this@GroupDetailsActivity,
                                    "Failed to update group",
                                    Toast.LENGTH_SHORT
                            )
                            .show()
                }
            }
        }

        dialog.show()
    }

    private fun showDeleteGroupDialog() {
        AlertDialog.Builder(this)
                .setTitle("Delete Group")
                .setMessage(
                        "Are you sure you want to delete this group? This action cannot be undone. All members will lose access."
                )
                .setPositiveButton("Delete") { _, _ -> deleteGroup() }
                .setNegativeButton("Cancel", null)
                .show()
    }

    private fun deleteGroup() {
        lifecycleScope.launch {
            try {
                val success = groupRepository.deleteGroup(groupId)
                if (success) {
                    Toast.makeText(
                                    this@GroupDetailsActivity,
                                    "Group deleted successfully",
                                    Toast.LENGTH_SHORT
                            )
                            .show()
                    finish() // Close activity and return to groups list
                } else {
                    Toast.makeText(
                                    this@GroupDetailsActivity,
                                    "Failed to delete group. Only owner can delete.",
                                    Toast.LENGTH_SHORT
                            )
                            .show()
                }
            } catch (e: Exception) {
                Toast.makeText(
                                this@GroupDetailsActivity,
                                "Error deleting group: ${e.message}",
                                Toast.LENGTH_SHORT
                        )
                        .show()
            }
        }
    }
}

data class Member(
        val userId: String,
        val name: String,
        val email: String,
        val isAdmin: Boolean,
        val isOnline: Boolean,
        val profileImageUrl: String = ""
)
