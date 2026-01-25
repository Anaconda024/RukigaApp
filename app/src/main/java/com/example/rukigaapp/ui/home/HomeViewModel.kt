package com.example.rukigaapp.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.rukigaapp.data.CategoryItem
import com.example.rukigaapp.data.DictionState
import com.example.rukigaapp.data.HomeState
import com.example.rukigaapp.data.QuizResult
import com.example.rukigaapp.data.QuizResultState
import com.example.rukigaapp.services.repositories.DictionRepository
import com.example.rukigaapp.services.repositories.QuizResultRepository
import com.example.rukigaapp.services.events.DictionEvent
import com.example.rukigaapp.services.events.QuizResultEvent
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class HomeViewModel (
    private val quizResultRepository: QuizResultRepository,
    private val dictionRepository: DictionRepository
) : ViewModel() {

    private val _state = MutableStateFlow(HomeState())
    private val _quizResults = quizResultRepository.allQuizResult
    private val _categories = MutableStateFlow<List<CategoryItem>>(emptyList())

    val state: StateFlow<HomeState> = combine(
        _state,
        _quizResults,
        _categories
    ) { state, quizResults, categories ->
        state.copy(
            quizResults = quizResults,
            categories = categories
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), HomeState())

    init {
        loadCategories()
    }

    private fun loadCategories() {
        viewModelScope.launch {
            try {
                val categoriesWithCounts = dictionRepository.getAllCategoriesWithCounts()
                _categories.value = categoriesWithCounts
            } catch (e: Exception) {
                _state.update { it.copy(errorMessage = "Failed to load categories: ${e.message}") }
            }
        }
    }

    fun onEvent(event: QuizResultEvent) {
        when (event) {
            is QuizResultEvent.LoadQuizResult -> {

            }


            // Add other home-specific events here if needed
            QuizResultEvent.ClearErrorMessage -> TODO()
            is QuizResultEvent.DeleteQuizResult -> TODO()
            QuizResultEvent.HideAddQuizResultDialog -> TODO()
            is QuizResultEvent.SaveQuizResult -> TODO()
            is QuizResultEvent.SetQuizResultAnsweredCorrect -> TODO()
            is QuizResultEvent.SetQuizResultAnsweredWrong -> TODO()
            is QuizResultEvent.SetQuizResultDateTaken -> TODO()
            is QuizResultEvent.SetQuizResultQuestionCount -> TODO()
            is QuizResultEvent.SetQuizResultQuizCategoryId -> TODO()
            is QuizResultEvent.SetQuizResultScore -> TODO()
            is QuizResultEvent.SetQuizResultUserId -> TODO()
            QuizResultEvent.ShowAddQuizResultDialog -> TODO()
            is QuizResultEvent.setQuizResultIsWritten -> TODO()
        }
    }
}