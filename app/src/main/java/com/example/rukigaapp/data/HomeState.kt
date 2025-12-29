package com.example.rukigaapp.data

data class HomeState(
    val quizResults: List<QuizResult> = emptyList(),
    val categories: List<CategoryItem> = emptyList(),
    val errorMessage: String? = null
)