package com.example.rukigaapp.ui.home

class QuizResultItem (
    val id: Int = 0, // Consider making it non-nullable if autoGenerate is true
    val dateTaken: String,
    var score: Int,
    val quizCategoryId: Int,
    var questionCount: Int,
    val isWritten: Boolean = false,
    val deleted: Boolean = false,
)