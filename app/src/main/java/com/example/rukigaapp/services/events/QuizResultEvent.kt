package com.example.rukigaapp.services.events

import com.example.rukigaapp.data.QuizResult

sealed interface QuizEvent {

    // Event to save a complete QuizResult
    data class SaveQuizResult(val quizResult: QuizResult) : QuizEvent

    // Events for updating specific fields of an existing or new QuizResult
    data class SetQuizResultDateTaken(val dateTaken: String) : QuizEvent
    data class SetQuizResultScore(val score: Int) : QuizEvent
    data class SetQuizResultQuizCategoryId(val quizCategoryId: Int) : QuizEvent
    data class SetQuizResultUserId(val userId: String?) : QuizEvent
    data class SetQuizResultQuestionCount(val questionCount: Int) : QuizEvent
    data class SetQuizResultAnsweredCorrect(val answeredCorrect: String?) : QuizEvent
    data class SetQuizResultAnsweredWrong(val answeredWrong: String?) : QuizEvent
    data class SetQuizResultDeleted(val deleted: Boolean) : QuizEvent

    // Event to delete a QuizResult by its ID
    data class DeleteQuizResult(val id: Int) : QuizEvent

    // Optional: Event to load a QuizResult (e.g., for editing)
    data class LoadQuizResult(val id: Int) : QuizEvent
}