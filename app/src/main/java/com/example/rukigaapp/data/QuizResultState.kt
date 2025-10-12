package com.example.rukigaapp.data

import kotlinx.datetime.LocalDateTime
import java.time.format.DateTimeFormatter

data class QuizResultState (

    val quizResults: List<QuizResult> = emptyList(),
    val id: Int = 0, // Consider making it non-nullable if autoGenerate is true
    val dateTaken: String = "",
    val score: Int = 0,
    val quizCategoryId: Int =0,
    val userId: String? = null,
    val questionCount: Int =10,
    val answeredCorrect: String? = null,
    val answeredWrong: String? = null,
    val isWritten: Boolean = false,
    val deleted: Boolean = false,

    val isAddingQuizResult: Boolean = false,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
)

