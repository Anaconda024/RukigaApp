package com.example.rukigaapp

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.rukigaapp.databinding.FragmentProfileBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import java.io.IOException

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private lateinit var auth: FirebaseAuth
    private var selectedImageUri: Uri? = null

    // Activity Result Launcher for picking image from gallery
    private val pickImageLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            selectedImageUri = it
            binding.avatarImage.setImageURI(it)
            // Here you can upload the image to Firebase Storage if needed
            if (isAdded && context != null) {
                Toast.makeText(requireContext(), "Avatar updated locally", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Activity Result Launcher for taking photo with camera
    private val takePictureLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val imageBitmap = result.data?.extras?.get("data") as? Bitmap
            imageBitmap?.let {
                binding.avatarImage.setImageBitmap(it)
                if (isAdded && context != null) {
                    Toast.makeText(requireContext(), "Avatar updated from camera", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance()

        // Load user data
        loadUserData()

        // Set up click listeners
        setupClickListeners()
    }

    private fun loadUserData() {
        val user = auth.currentUser

        user?.let {
            // Load username
            binding.usernameInput.setText(it.displayName ?: "User")

            // Load email
            binding.emailInput.setText(it.email ?: "")

            // Load profile picture if available
            it.photoUrl?.let { photoUrl ->
                // You can use Glide or Picasso to load the image
                // For now, we'll just show the default avatar
                // Glide.with(this).load(photoUrl).into(binding.avatarImage)
            }
        }
    }

    private fun setupClickListeners() {
        // Back button
        binding.backButton.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

        // Edit avatar button
        binding.editAvatarButton.setOnClickListener {
            showAvatarOptions()
        }

        // Avatar container click
        binding.avatarContainer.setOnClickListener {
            showAvatarOptions()
        }

        // Save username button
        binding.saveUsernameButton.setOnClickListener {
            saveUsername()
        }

        // Reset password button
        binding.resetPasswordButton.setOnClickListener {
            resetPassword()
        }

        // Logout button
        binding.logoutButton.setOnClickListener {
            showLogoutConfirmation()
        }
    }

    private fun showAvatarOptions() {
        val options = arrayOf("Take Photo", "Choose from Gallery", "Cancel")

        AlertDialog.Builder(requireContext())
            .setTitle("Change Avatar")
            .setItems(options) { dialog, which ->
                when (which) {
                    0 -> takePhoto()
                    1 -> pickFromGallery()
                    2 -> dialog.dismiss()
                }
            }
            .show()
    }

    private fun takePhoto() {
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (takePictureIntent.resolveActivity(requireActivity().packageManager) != null) {
            takePictureLauncher.launch(takePictureIntent)
        } else {
            Toast.makeText(requireContext(), "Camera not available", Toast.LENGTH_SHORT).show()
        }
    }

    private fun pickFromGallery() {
        pickImageLauncher.launch("image/*")
    }

    private fun saveUsername() {
        val newUsername = binding.usernameInput.text.toString().trim()

        if (newUsername.isEmpty()) {
            binding.usernameInput.error = "Username cannot be empty"
            return
        }

        if (newUsername.length < 3) {
            binding.usernameInput.error = "Username must be at least 3 characters"
            return
        }

        // Show loading
        binding.saveUsernameButton.isEnabled = false
        binding.saveUsernameButton.text = "Saving..."

        val user = auth.currentUser
        val profileUpdates = UserProfileChangeRequest.Builder()
            .setDisplayName(newUsername)
            .build()

        user?.updateProfile(profileUpdates)
            ?.addOnCompleteListener { task ->
                if (isAdded && context != null) {
                    binding.saveUsernameButton.isEnabled = true
                    binding.saveUsernameButton.text = getString(R.string.save)

                    if (task.isSuccessful) {
                        Toast.makeText(
                            requireContext(),
                            "Username updated successfully",
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        Toast.makeText(
                            requireContext(),
                            "Failed to update username: ${task.exception?.message}",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            }
    }

    private fun resetPassword() {
        val email = auth.currentUser?.email

        if (email.isNullOrEmpty()) {
            Toast.makeText(
                requireContext(),
                "No email associated with this account",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        AlertDialog.Builder(requireContext())
            .setTitle("Reset Password")
            .setMessage("A password reset link will be sent to $email. Do you want to continue?")
            .setPositiveButton("Send") { _, _ ->
                sendPasswordResetEmail(email)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun sendPasswordResetEmail(email: String) {
        // Show loading
        binding.resetPasswordButton.isEnabled = false
        binding.resetPasswordButton.text = "Sending..."

        auth.sendPasswordResetEmail(email)
            .addOnCompleteListener { task ->
                if (isAdded && context != null) {
                    binding.resetPasswordButton.isEnabled = true
                    binding.resetPasswordButton.text = getString(R.string.reset_password)

                    if (task.isSuccessful) {
                        AlertDialog.Builder(requireContext())
                            .setTitle("Email Sent")
                            .setMessage("Password reset email has been sent to $email. Please check your inbox.")
                            .setPositiveButton("OK", null)
                            .show()
                    } else {
                        Toast.makeText(
                            requireContext(),
                            "Failed to send reset email: ${task.exception?.message}",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            }
    }

    private fun showLogoutConfirmation() {
        AlertDialog.Builder(requireContext())
            .setTitle("Logout")
            .setMessage("Are you sure you want to logout?")
            .setPositiveButton("Logout") { _, _ ->
                logout()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun logout() {
        // Sign out from Firebase
        auth.signOut()

        findNavController().navigate(R.id.action_profileFragment_to_loginFragment)

        // Navigate to login screen
        try {
        } catch (e: Exception) {
            // If navigation fails, just log it
            e.printStackTrace()
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}