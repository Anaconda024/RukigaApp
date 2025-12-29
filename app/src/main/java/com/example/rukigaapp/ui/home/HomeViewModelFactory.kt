package com.example.rukigaapp.ui.home // Or wherever your HomeFragment is

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.rukigaapp.services.DictionRepository
import com.example.rukigaapp.services.QuizResultRepository

// The factory takes any dependencies the ViewModel needs as arguments
class HomeViewModelFactory(
    private val quizResultRepository: QuizResultRepository,
    private val dictionRepository: DictionRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HomeViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return HomeViewModel(quizResultRepository, dictionRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }

}
    