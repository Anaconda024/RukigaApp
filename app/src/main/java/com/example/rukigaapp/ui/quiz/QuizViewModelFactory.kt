package com.example.rukigaapp.ui.quiz

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.rukigaapp.services.QuizResultRepository

class QuizViewModelFactory(
    private val repository: QuizResultRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(QuizViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return QuizViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}