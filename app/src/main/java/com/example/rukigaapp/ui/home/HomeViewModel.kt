package com.example.rukigaapp.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.rukigaapp.data.DictionState
import com.example.rukigaapp.data.QuizResult
import com.example.rukigaapp.data.QuizResultState
import com.example.rukigaapp.services.DictionRepository
import com.example.rukigaapp.services.QuizResultRepository
import com.example.rukigaapp.services.events.DictionEvent
import com.example.rukigaapp.services.events.QuizResultEvent
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update

class HomeViewModel (
    private val quizResultRepository: QuizResultRepository
): ViewModel() {
    private val _state = MutableStateFlow(QuizResultState())
    private val _quizResult = quizResultRepository.allQuizResult

    // The combine function now only takes two flows and a function that accepts two arguments
    val state = combine(_state, _quizResult, ::mergeState)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), QuizResultState())

    // ... rest of your onEvent and other ViewModel logic

    // --- THIS IS THE FUNCTION TO FIX ---
    // Update the mergeState function to only accept two parameters
    private fun mergeState(
        state: QuizResultState,
        quizResult: List<QuizResult>
    ): QuizResultState {
        // Now it correctly combines the UI state with the list of results from the database
        return state.copy(
            quizResults = quizResult
        )
    }

    fun onEvent(event: QuizResultEvent) {
        when (event) {
            is QuizResultEvent.LoadQuizResult -> {
                _state.update { it.copy(
                    isAddingQuizResult = true,
                    id = event.quizResult.id,
                )
                }
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