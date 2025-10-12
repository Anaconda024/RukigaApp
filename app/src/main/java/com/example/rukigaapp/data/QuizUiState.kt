package com.example.rukigaapp.data

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

// Assuming QuizQuestion is defined elsewhere. If not, you'll need a placeholder or actual definition.
// Example placeholder if QuizQuestion is not yet defined:
// data class QuizQuestion(val text: String, val options: List<String>, val correctAnswerIndex: Int)

data class QuizUiState(
    val currentQuestion: QuizQuestion? = null, // Provide default if possible
    val currentIndex: Int = 0,
    val totalQuestions: Int = 0,
    val isFinished: Boolean = false
)

// Create a private MutableStateFlow that will hold the current state.
// Initialize it with a default/initial instance of QuizUiState.
private val _uiState = MutableStateFlow(
    QuizUiState(
        currentQuestion = null, // Or some initial question if available
        currentIndex = 0,
        totalQuestions = 0,     // Or a sensible default, e.g., from a loaded quiz
        isFinished = false
    )
)

// Expose the MutableStateFlow as a read-only StateFlow for observers.
val uiState: StateFlow<QuizUiState> = _uiState.asStateFlow()