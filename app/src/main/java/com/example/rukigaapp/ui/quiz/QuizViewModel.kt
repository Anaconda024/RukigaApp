package com.example.rukigaapp.ui.quiz

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.rukigaapp.data.Diction
import com.example.rukigaapp.data.DictionState
import com.example.rukigaapp.data.QuizConfig
import com.example.rukigaapp.data.QuizQuestion
import com.example.rukigaapp.data.QuizResult
import com.example.rukigaapp.data.QuizResultState
import com.example.rukigaapp.data.QuizUiState
import com.example.rukigaapp.data.SortType
import com.example.rukigaapp.services.DictionRepository
import com.example.rukigaapp.services.QuizResultRepository
import com.example.rukigaapp.services.events.QuizResultEvent
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.text.isBlank

class QuizViewModel(
    private val resultRepository: QuizResultRepository,
    private val dictionRepository: DictionRepository
) : ViewModel() {
    private val _state = MutableStateFlow(QuizResultState())
    private val _quizResults = resultRepository.allQuizResult
    private var _quizQuestionsState =
        MutableStateFlow<List<QuizQuestion>>(emptyList()) // Or a more specific state
    val quizQuestionsState: StateFlow<List<QuizQuestion>> = _quizQuestionsState.asStateFlow()

    // Change to MutableList and initialize them
    public var answeredCorrect: MutableList<Int> = mutableListOf()
        private set // Optional: Make setter private if modification should only happen internally

    public var answeredWrong: MutableList<Int> = mutableListOf()
        private set // Optional: Make setter private
    private val _uiState = MutableStateFlow(QuizUiState()) // Initial default state
    val uiState: StateFlow<QuizUiState> = _uiState.asStateFlow()
    public var currentQuestionIndex: Int = 0

    public var quizConfig: QuizConfig? = null
    public var currentQuestion: QuizQuestion? = null
        get() = quizQuestionsState.value.getOrNull(
            currentQuestionIndex
        )

    public fun setQuizConfig(quizCategoryI: Int, numberOfQuestions: Int, isWritten: Boolean) {
        quizConfig = QuizConfig(
            quizCategoryId = quizCategoryI,
            userId = "Ankunda",
            numberOfQuestions = numberOfQuestions,
            isWritten = isWritten,
            dictionCategoryId = null
        )
        getQuizQuestions(quizConfig!!)
        currentQuestion = quizQuestionsState.value.getOrNull(
            currentQuestionIndex
        )
    }


    fun startNewQuiz() {
        answeredCorrect.clear()
        answeredWrong.clear()
        currentQuestionIndex = 0
        // ... other quiz initialization logic ...
    }

    val state = combine(_state, _quizResults) { uiState, allResults ->
        uiState.copy(quizResults = allResults)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = QuizResultState() // Initial value for the combined flow
    )


    fun onEvent(event: QuizResultEvent) {
        when (event) {
            is QuizResultEvent.SetQuizResultDateTaken -> {
                _state.update { it.copy(dateTaken = event.dateTaken) }
            }

            is QuizResultEvent.SetQuizResultScore -> {
                _state.update { it.copy(score = event.score) }
            }

            is QuizResultEvent.SetQuizResultQuizCategoryId -> {
                _state.update { it.copy(quizCategoryId = event.quizCategoryId) }
            }

            is QuizResultEvent.SetQuizResultUserId -> {
                _state.update { it.copy(userId = event.userId) }
            }

            is QuizResultEvent.SetQuizResultQuestionCount -> {
                _state.update { it.copy(questionCount = event.questionCount) }
            }

            is QuizResultEvent.SetQuizResultAnsweredCorrect -> {
                _state.update { it.copy(answeredCorrect = event.answeredCorrect) }
            }

            is QuizResultEvent.SetQuizResultAnsweredWrong -> {
                _state.update { it.copy(answeredWrong = event.answeredWrong) }
            }

            is QuizResultEvent.setQuizResultIsWritten -> {
                _state.update { it.copy(isWritten = event.isWritten) }
            }

            is QuizResultEvent.SaveQuizResult -> {
                val currentState = _state.value
                // Use quizResultToSaveInitially if provided (e.g. from an edit path wher e you directly pass the whole object)
                // Otherwise, construct from the current state.
                val quizResultToSave = event.quizResult ?: QuizResult(
                    id = currentState.id, // Use id from state for updates
                    dateTaken = currentState.dateTaken,
                    score = currentState.score,
                    quizCategoryId = currentState.quizCategoryId,
                    userId = currentState.userId,
                    questionCount = currentState.questionCount,
                    answeredCorrect = currentState.answeredCorrect,
                    answeredWrong = currentState.answeredWrong,
                    // isWritten = currentState.isWritten, // Include if isWritten is part of QuizResult entity
                    deleted = false
                )
                saveQuizResultToDb(quizResultToSave)
                // Reset form fields in state and hide dialog
                _state.update {
                    it.copy(
                        isAddingQuizResult = false,
                        id = 0,
                        dateTaken = "",
                        score = 0,
                        quizCategoryId = 0,
                        userId = "",
                        questionCount = 0,
                        answeredCorrect = "",
                        answeredWrong = "",
                        isWritten = false,
                        errorMessage = null
                    )
                }
            }

            is QuizResultEvent.DeleteQuizResult -> {
                viewModelScope.launch {
                    resultRepository.softDeleteQuizResult(event.id)
                }
            }

            is QuizResultEvent.LoadQuizResult -> {
                _state.update {
                    it.copy(
                        id = event.quizResult.id,
                        dateTaken = event.quizResult.dateTaken,
                        score = event.quizResult.score,
                        quizCategoryId = event.quizResult.quizCategoryId,
                        userId = event.quizResult.userId,
                        questionCount = event.quizResult.questionCount,
                        answeredCorrect = event.quizResult.answeredCorrect,
                        answeredWrong = event.quizResult.answeredWrong,
                        isWritten = event.quizResult.isWritten, // Populate if applicable
                        isAddingQuizResult = true,
                        errorMessage = null
                    )
                }
            }

            QuizResultEvent.ShowAddQuizResultDialog -> {
                _state.update {
                    it.copy(
                        isAddingQuizResult = true,
                        id = 0, // Reset for new entry
                        dateTaken = "",
                        score = 0,
                        quizCategoryId = 0,
                        userId = "",
                        questionCount = 0,
                        answeredCorrect = "",
                        answeredWrong = "",
                        isWritten = false,
                        errorMessage = null
                    )
                }
            }

            QuizResultEvent.HideAddQuizResultDialog -> {
                _state.update { it.copy(isAddingQuizResult = false, errorMessage = null) }
            }

            QuizResultEvent.ClearErrorMessage -> {
                _state.update { it.copy(errorMessage = null) }
            }
        }
    }

    public fun saveQuizResultToDb(quizResult: QuizResult) {
        viewModelScope.launch {
            if (quizResult.userId!!.isBlank()) {
                _state.update { it.copy(errorMessage = "User ID cannot be empty.") }
                return@launch
            }
            if (quizResult.dateTaken.isBlank()) {
                _state.update { it.copy(errorMessage = "Date taken cannot be empty.") }
                return@launch
            }
            // Add more validation...

            resultRepository.upsert(quizResult)
        }
    }

    fun getQuizQuestions(quizConfig: QuizConfig) {
        viewModelScope.launch {
            try {
                dictionRepository.getDictionForQuiz(quizConfig.dictionCategoryId, quizConfig.numberOfQuestions)
                    .map { dictionList -> dictionList.toQuizQuestions(quizConfig.quizCategoryId) }
                    .collect { questions ->
                        _quizQuestionsState.value = questions
                    }
            } catch (e: Exception) {
                // Handle error
                _quizQuestionsState.value = emptyList()
            }
        }
    }

    private fun List<Diction>.toQuizQuestions(quizCategoryId: Int): List<QuizQuestion> {
        return when (quizCategoryId) {
            1 -> map { diction ->
                QuizQuestion(
                    QuestionId = diction.id,
                    Question = diction.rukiga,
                    CorrectAnswer = diction.english,
                )
            }
            2 -> map { diction ->
                QuizQuestion(
                    QuestionId = diction.id,
                    Question = diction.english,
                    CorrectAnswer = diction.rukiga,
                )
            }
            else -> map { diction ->
                QuizQuestion(
                    QuestionId = diction.id,
                    Question = diction.rukiga,
                    CorrectAnswer = diction.english,
                )
            }
        }
    }

    private fun saveAnswer(isCorrect: Boolean, questionId: Int) { // Renamed parameter for clarity
        if (isCorrect) {
            if (!answeredCorrect.contains(questionId)) { // Optional: Avoid duplicates
                answeredCorrect.add(questionId)
            }
        } else {
            if (!answeredWrong.contains(questionId)) { // Optional: Avoid duplicates
                answeredWrong.add(questionId)
            }
        }
    }

    public fun markInput(answer: String, quizQuestion: QuizQuestion) {
        if (answer == quizQuestion.CorrectAnswer) {
            saveAnswer(true, quizQuestion.QuestionId)
        } else {
            saveAnswer(false, quizQuestion.QuestionId)
        }
    }

    fun updateCurrentQuestion(question: QuizQuestion, newIndex: Int, total: Int) {
        // currentQuestion = quizQuestionsState[newIndex] // This line had an issue, see explanation below
        // You should update _uiState with the new current question from the list
        val questions = _quizQuestionsState.value
        if (newIndex >= 0 && newIndex < questions.size) {
            _uiState.update {
                it.copy(
                    currentQuestion = questions[newIndex], // Get from the list
                    currentIndex = newIndex,
                    totalQuestions = total,
                    isFinished = newIndex >= total - 1
                )
            }
            currentQuestionIndex = newIndex // Update your tracking index
        } else if (newIndex >= total - 1) { // Handle finishing the quiz
            _uiState.update {
                it.copy(
                    currentQuestion = null, // Or the last question
                    currentIndex = newIndex,
                    totalQuestions = total,
                    isFinished = true
                )
            }
            currentQuestionIndex = newIndex
        }
    }

}