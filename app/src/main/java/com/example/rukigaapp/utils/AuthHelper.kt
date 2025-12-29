package com.example.rukigaapp.utils

import android.content.Context
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth

/**
 * Authentication Helper Class
 * Provides utility functions for authentication-related operations
 */
object AuthHelper {

    /**
     * Check if user is currently logged in
     */
    fun isUserLoggedIn(): Boolean {
        return FirebaseAuth.getInstance().currentUser != null
    }

    /**
     * Get current user's email
     */
    fun getCurrentUserEmail(): String? {
        return FirebaseAuth.getInstance().currentUser?.email
    }

    /**
     * Get current user's display name
     */
    fun getCurrentUserDisplayName(): String? {
        return FirebaseAuth.getInstance().currentUser?.displayName
    }

    /**
     * Get current user's ID
     */
    fun getCurrentUserId(): String? {
        return FirebaseAuth.getInstance().currentUser?.uid
    }

    /**
     * Check if current user's email is verified
     */
    fun isEmailVerified(): Boolean {
        return FirebaseAuth.getInstance().currentUser?.isEmailVerified ?: false
    }

    /**
     * Sign out the current user and navigate to login screen
     * @param navController Navigation controller to handle navigation
     * @param loginDestinationId The ID of the login fragment destination
     */
    fun signOut(navController: NavController, loginDestinationId: Int) {
        FirebaseAuth.getInstance().signOut()

        // Navigate to login and clear back stack
        navController.navigate(loginDestinationId) {
            // Pop everything up to and including the current destination
            popUpTo(0) {
                inclusive = true
            }
        }
    }

    /**
     * Sign out without navigation (useful when you want to handle navigation separately)
     */
    fun signOutOnly() {
        FirebaseAuth.getInstance().signOut()
    }
}