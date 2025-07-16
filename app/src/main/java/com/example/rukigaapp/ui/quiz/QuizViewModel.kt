package com.example.rukigaapp.ui.quiz

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.rukigaapp.services.QuizResultRepository

class QuizViewModel(
    private val repository: QuizResultRepository
) : ViewModel() {

    private val _diction = repository.allQuizResult
}