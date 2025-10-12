package com.example.rukigaapp.ui.quiz

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.get
import com.example.rukigaapp.services.DictionRepository
import com.example.rukigaapp.services.QuizResultRepository

class QuizViewModelFactory(
    private val repository: QuizResultRepository,
    private val dictionrepository: DictionRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(QuizViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return QuizViewModel(repository, dictionrepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}