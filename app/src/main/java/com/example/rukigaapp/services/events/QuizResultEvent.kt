package com.example.rukigaapp.services.events

import com.example.rukigaapp.data.QuizResult

sealed interface QuizResultEvent {

    // Event to save a complete QuizResult
    data class SaveQuizResult(val quizResult: QuizResult) : QuizResultEvent

    // Events for updating specific fields of an existing or new QuizResult
    data class SetQuizResultDateTaken(val dateTaken: String) : QuizResultEvent
    data class SetQuizResultScore(val score: Int) : QuizResultEvent
    data class SetQuizResultQuizCategoryId(val quizCategoryId: Int) : QuizResultEvent
    data class SetQuizResultUserId(val userId: String?) : QuizResultEvent
    data class SetQuizResultQuestionCount(val questionCount: Int) : QuizResultEvent
    data class SetQuizResultAnsweredCorrect(val answeredCorrect: String?) : QuizResultEvent
    data class SetQuizResultAnsweredWrong(val answeredWrong: String?) : QuizResultEvent
    data class setQuizResultIsWritten(val isWritten: Boolean) : QuizResultEvent
    data class SetQuizResultDeleted(val deleted: Boolean) : QuizResultEvent

    // Event to delete a QuizResult by its ID
    data class DeleteQuizResult(val id: Int) : QuizResultEvent

    // Optional: Event to load a QuizResult (e.g., for editing)
    data class LoadQuizResult(val id: Int) : QuizResultEvent
}