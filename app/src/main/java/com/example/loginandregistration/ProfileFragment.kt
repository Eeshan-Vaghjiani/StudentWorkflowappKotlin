package com.example.loginandregistration

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.loginandregistration.databinding.FragmentProfileBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private lateinit var auth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = Firebase.auth
        val currentUser = auth.currentUser

        updateUI(currentUser)

        binding.btnLogout.setOnClickListener {
            logoutUser()
        }
    }

    private fun updateUI(user: FirebaseUser?) {
        if (user != null) {
            binding.tvProfileEmail.text = user.email ?: "N/A"
            binding.tvProfileUid.text = user.uid
        } else {
            binding.tvProfileEmail.text = "Not logged in"
            binding.tvProfileUid.text = "N/A"
            binding.btnLogout.isEnabled = false
        }
    }

    private fun logoutUser() {
        auth.signOut()

        // Sign out from Google as well if the user signed in with Google
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id)) // Ensure this string exists
            .requestEmail()
            .build()
        val googleSignInClient = GoogleSignIn.getClient(requireActivity(), gso)
        googleSignInClient.signOut().addOnCompleteListener {
            // Regardless of Google sign-out success, navigate to Login
            navigateToLoginScreen()
        }
    }

    private fun navigateToLoginScreen() {
        Toast.makeText(requireContext(), "Logged out", Toast.LENGTH_SHORT).show()
        val intent = Intent(requireActivity(), Login::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        requireActivity().finish()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}