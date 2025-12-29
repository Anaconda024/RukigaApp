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
import com.example.rukigaapp.databinding.FragmentLoginBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider

class LoginFragment : Fragment() {

    private var _binding: FragmentLoginBinding? = null
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
            Toast.makeText(requireContext(), "Google sign-in cancelled", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
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
        // Login button click
        binding.loginButton.setOnClickListener {
            val email = binding.emailEditText.text.toString().trim()
            val password = binding.passwordEditText.text.toString().trim()

            if (validateInputs(email, password)) {
                loginWithEmail(email, password)
            }
        }

        // Google Sign-In button click
        binding.googleSignUp.setOnClickListener {
            signInWithGoogle()
        }

        // Password visibility toggle
        binding.passwordVisibilityToggle.setOnClickListener {
            togglePasswordVisibility()
        }

        // Forgot password click
        binding.forgotPassword.setOnClickListener {
            handleForgotPassword()
        }
    }

    private fun setupFooterText() {
        val footerText = "Don't have an account? Create Account"
        val spannableString = SpannableString(footerText)

        val clickableSpan = object : ClickableSpan() {
            override fun onClick(widget: View) {
                navigateToRegister()
            }
        }

        val startIndex = footerText.indexOf("Create Account")
        val endIndex = startIndex + "Create Account".length

        spannableString.setSpan(
            clickableSpan,
            startIndex,
            endIndex,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        // Set color for "Create Account"
        val colorPrimary = ContextCompat.getColor(requireContext(),com.example.rukigaapp.R.color.colorPrimary)
        spannableString.setSpan(
            ForegroundColorSpan(colorPrimary),
            startIndex,
            endIndex,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        binding.footerText.text = spannableString
        binding.footerText.movementMethod = LinkMovementMethod.getInstance()
    }

    private fun validateInputs(email: String, password: String): Boolean {
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

        return true
    }

    private fun loginWithEmail(email: String, password: String) {
        showLoading()

        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(requireActivity()) { task ->
                hideLoading()

                if (task.isSuccessful) {
                    // Sign in success
                    val user = auth.currentUser
                    if (isAdded && context != null) {
                        Toast.makeText(
                            requireContext(),
                            "Welcome back, ${user?.email}!",
                            Toast.LENGTH_SHORT
                        ).show()

                        // Navigate to main app
                        navigateToMainApp()
                    }
                } else {
                    // Sign in failed
                    if (isAdded && context != null) {
                        val errorMessage = when {
                            task.exception?.message?.contains("password") == true ->
                                "Incorrect password. Please try again."
                            task.exception?.message?.contains("user") == true ->
                                "No account found with this email."
                            task.exception?.message?.contains("network") == true ->
                                "Network error. Please check your connection."
                            else ->
                                "Login failed: ${task.exception?.message}"
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

    private fun signInWithGoogle() {
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
                "Google sign-in failed: ${e.message}",
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
                            "Account created successfully!"
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

    private fun handleForgotPassword() {
        val email = binding.emailEditText.text.toString().trim()

        if (email.isEmpty()) {
            Toast.makeText(
                requireContext(),
                "Please enter your email address first",
                Toast.LENGTH_SHORT
            ).show()
            binding.emailEditText.requestFocus()
            return
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(
                requireContext(),
                "Please enter a valid email address",
                Toast.LENGTH_SHORT
            ).show()
            binding.emailEditText.requestFocus()
            return
        }

        showLoading()

        auth.sendPasswordResetEmail(email)
            .addOnCompleteListener { task ->
                hideLoading()

                if (task.isSuccessful) {
                    Toast.makeText(
                        requireContext(),
                        "Password reset email sent to $email",
                        Toast.LENGTH_LONG
                    ).show()
                } else {
                    Toast.makeText(
                        requireContext(),
                        "Failed to send reset email: ${task.exception?.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
    }

    private fun navigateToRegister() {
        // Navigate to RegisterFragment
        // Update this with your actual navigation action
        findNavController().navigate(com.example.rukigaapp.R.id.action_loginFragment_to_registerFragment)
    }

    private fun navigateToMainApp() {
        // Navigate to main app screen
        // Update this with your actual navigation action
        findNavController().navigate(com.example.rukigaapp.R.id.action_loginFragment_to_nav_home)
    }

    private fun showLoading() {
        binding.loginButton.isEnabled = false
        binding.googleSignUp.isEnabled = false
        binding.loginButton.text = "Loading..."
    }

    private fun hideLoading() {
        binding.loginButton.isEnabled = true
        binding.googleSignUp.isEnabled = true
        binding.loginButton.text = "Login"
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}