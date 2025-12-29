package com.example.rukigaapp.ui.authentication

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.text.SpannableString
import android.text.Spanned
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.ForegroundColorSpan
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.rukigaapp.databinding.FragmentRegisterBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.UserProfileChangeRequest

class RegisterFragment : Fragment() {

    private var _binding: FragmentRegisterBinding? = null
    private val binding get() = _binding!!

    private lateinit var auth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient
    private var isPasswordVisible = false

    // Activity Result Launcher for Google Sign-In
    private val googleSignInLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            handleGoogleSignInResult(task)
        } else {
            hideLoading()
            Toast.makeText(requireContext(), "Google sign-up cancelled", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRegisterBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance()

        // Configure Google Sign-In
        configureGoogleSignIn()

        // Set up UI listeners
        setupClickListeners()
        setupFooterText()
    }

    private fun configureGoogleSignIn() {
        // Configure Google Sign-In to request user's ID, email, and basic profile
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(com.example.rukigaapp.R.string.default_web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(requireActivity(), gso)
    }

    private fun setupClickListeners() {
        // Back button click
        binding.backButton.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

        // Register button click
        binding.registerButton.setOnClickListener {
            val username = binding.usernameEditText.text.toString().trim()
            val email = binding.emailEditText.text.toString().trim()
            val password = binding.passwordEditText.text.toString().trim()

            if (validateInputs(username, email, password)) {
                registerWithEmail(username, email, password)
            }
        }

        // Google Sign-Up button click
        binding.googleSignUp.setOnClickListener {
            signUpWithGoogle()
        }

        // Password visibility toggle
        binding.passwordVisibilityToggle.setOnClickListener {
            togglePasswordVisibility()
        }
    }

    private fun setupFooterText() {
        val footerText = "Already have an account? Log In"
        val spannableString = SpannableString(footerText)

        val clickableSpan = object : ClickableSpan() {
            override fun onClick(widget: View) {
                navigateToLogin()
            }
        }

        val startIndex = footerText.indexOf("Log In")
        val endIndex = startIndex + "Log In".length

        spannableString.setSpan(
            clickableSpan,
            startIndex,
            endIndex,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        // Set color for "Log In"
        val colorPrimary = ContextCompat.getColor(requireContext(), com.example.rukigaapp.R.color.colorPrimary)
        spannableString.setSpan(
            ForegroundColorSpan(colorPrimary),
            startIndex,
            endIndex,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        binding.alreadyRegistered.text = spannableString
        binding.alreadyRegistered.movementMethod = LinkMovementMethod.getInstance()
    }

    private fun validateInputs(username: String, email: String, password: String): Boolean {
        // Validate username
        if (username.isEmpty()) {
            binding.usernameEditText.error = "Username is required"
            binding.usernameEditText.requestFocus()
            return false
        }

        if (username.length < 3) {
            binding.usernameEditText.error = "Username must be at least 3 characters"
            binding.usernameEditText.requestFocus()
            return false
        }

        // Validate email
        if (email.isEmpty()) {
            binding.emailEditText.error = "Email is required"
            binding.emailEditText.requestFocus()
            return false
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.emailEditText.error = "Please enter a valid email"
            binding.emailEditText.requestFocus()
            return false
        }

        // Validate password
        if (password.isEmpty()) {
            binding.passwordEditText.error = "Password is required"
            binding.passwordEditText.requestFocus()
            return false
        }

        if (password.length < 6) {
            binding.passwordEditText.error = "Password must be at least 6 characters"
            binding.passwordEditText.requestFocus()
            return false
        }

        // Check password strength
        if (!password.matches(Regex(".*[A-Z].*"))) {
            binding.passwordEditText.error = "Password must contain at least one uppercase letter"
            binding.passwordEditText.requestFocus()
            return false
        }

        if (!password.matches(Regex(".*[a-z].*"))) {
            binding.passwordEditText.error = "Password must contain at least one lowercase letter"
            binding.passwordEditText.requestFocus()
            return false
        }

        if (!password.matches(Regex(".*\\d.*"))) {
            binding.passwordEditText.error = "Password must contain at least one digit"
            binding.passwordEditText.requestFocus()
            return false
        }

        return true
    }

    private fun registerWithEmail(username: String, email: String, password: String) {
        showLoading()

        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(requireActivity()) { task ->
                if (task.isSuccessful) {
                    // Registration success, update user profile with username
                    val user = auth.currentUser
                    val profileUpdates = UserProfileChangeRequest.Builder()
                        .setDisplayName(username)
                        .build()

                    user?.updateProfile(profileUpdates)
                        ?.addOnCompleteListener { profileTask ->
                            hideLoading()

                            if (profileTask.isSuccessful) {
                                // Send email verification
                                user.sendEmailVerification()
                                    ?.addOnCompleteListener { emailTask ->
                                        if (emailTask.isSuccessful && isAdded && context != null) {
                                            Toast.makeText(
                                                requireContext(),
                                                "Account created! Please verify your email.",
                                                Toast.LENGTH_LONG
                                            ).show()
                                        }
                                    }

                                if (isAdded && context != null) {
                                    Toast.makeText(
                                        requireContext(),
                                        "Welcome, $username!",
                                        Toast.LENGTH_SHORT
                                    ).show()

                                    // Navigate to main app
                                    navigateToMainApp()
                                }
                            } else {
                                if (isAdded && context != null) {
                                    Toast.makeText(
                                        requireContext(),
                                        "Profile update failed: ${profileTask.exception?.message}",
                                        Toast.LENGTH_LONG
                                    ).show()
                                }
                            }
                        }
                } else {
                    hideLoading()

                    // Registration failed
                    if (isAdded && context != null) {
                        val errorMessage = when {
                            task.exception?.message?.contains("already in use") == true ->
                                "This email is already registered. Please log in instead."
                            task.exception?.message?.contains("weak password") == true ->
                                "Password is too weak. Please use a stronger password."
                            task.exception?.message?.contains("network") == true ->
                                "Network error. Please check your connection."
                            else ->
                                "Registration failed: ${task.exception?.message}"
                        }

                        Toast.makeText(
                            requireContext(),
                            errorMessage,
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            }
    }

    private fun signUpWithGoogle() {
        showLoading()
        val signInIntent = googleSignInClient.signInIntent
        googleSignInLauncher.launch(signInIntent)
    }

    private fun handleGoogleSignInResult(completedTask: Task<GoogleSignInAccount>) {
        try {
            val account = completedTask.getResult(ApiException::class.java)
            // Signed in successfully, authenticate with Firebase
            firebaseAuthWithGoogle(account.idToken!!)
        } catch (e: ApiException) {
            // Google Sign-In failed
            hideLoading()
            Toast.makeText(
                requireContext(),
                "Google sign-up failed: ${e.message}",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)

        auth.signInWithCredential(credential)
            .addOnCompleteListener(requireActivity()) { task ->
                hideLoading()

                if (task.isSuccessful) {
                    // Sign in success
                    val user = auth.currentUser
                    val isNewUser = task.result?.additionalUserInfo?.isNewUser ?: false

                    if (isAdded && context != null) {
                        val message = if (isNewUser) {
                            "Account created successfully! Welcome, ${user?.displayName}!"
                        } else {
                            "Welcome back, ${user?.displayName}!"
                        }

                        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()

                        // Navigate to main app
                        navigateToMainApp()
                    }
                } else {
                    // Sign in failed
                    if (isAdded && context != null) {
                        Toast.makeText(
                            requireContext(),
                            "Authentication failed: ${task.exception?.message}",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            }
    }

    private fun togglePasswordVisibility() {
        if (isPasswordVisible) {
            // Hide password
            binding.passwordEditText.inputType =
                InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            binding.passwordVisibilityToggle.setImageResource(com.example.rukigaapp.R.drawable.ic_eye_off_cross)
        } else {
            // Show password
            binding.passwordEditText.inputType =
                InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            binding.passwordVisibilityToggle.setImageResource(com.example.rukigaapp.R.drawable.ic_eye)
        }

        // Move cursor to end of text
        binding.passwordEditText.setSelection(binding.passwordEditText.text.length)
        isPasswordVisible = !isPasswordVisible
    }

    private fun navigateToLogin() {
        // Navigate back to LoginFragment
        requireActivity().onBackPressedDispatcher.onBackPressed()
        // Or use: findNavController().navigate(com.example.rukigaapp.R.id.action_registerFragment_to_loginFragment)
    }

    private fun navigateToMainApp() {
        // Navigate to main app screen
        // Update this with your actual navigation action
        findNavController().navigate(com.example.rukigaapp.R.id.action_registerFragment_to_nav_home)
    }

    private fun showLoading() {
        binding.registerButton.isEnabled = false
        binding.googleSignUp.isEnabled = false
        binding.registerButton.text = "Creating Account..."
    }

    private fun hideLoading() {
        binding.registerButton.isEnabled = true
        binding.googleSignUp.isEnabled = true
        binding.registerButton.text = "Register"
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}