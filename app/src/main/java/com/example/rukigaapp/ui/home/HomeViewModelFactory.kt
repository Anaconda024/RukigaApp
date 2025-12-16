package com.example.rukigaapp.ui.home // Or wherever your HomeFragment is

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.rukigaapp.services.QuizResultRepository

// The factory takes any dependencies the ViewModel needs as arguments
class HomeViewModelFactory(
    private val repository: QuizResultRepository
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        // Check if the requested ViewModel is the one this factory knows how to create
        if (modelClass.isAssignableFrom(HomeViewModel::class.java)) {
            // If it is, create and return an instance of it
            @Suppress("UNCHECKED_CAST")
            return HomeViewModel(repository) as T
        }
        // If it's not, throw an exception. This is a crucial part of the contract.
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
    