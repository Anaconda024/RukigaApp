package com.example.rukigaapp.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "quizCategories")
data class QuizCategory(
    @PrimaryKey(autoGenerate = true)
    val id: Int, // Consider making it non-nullable if autoGenerate is true
    val name: String,
    val description: String?,
    val deleted: Boolean = false,
)