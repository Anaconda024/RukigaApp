package com.example.rukigaapp.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant

@Entity(tableName = "quizResults",
    foreignKeys = [
        ForeignKey(
            entity = QuizCategory::class,
            parentColumns = ["id"],
            childColumns = ["quizCategoryId"],
            onDelete = ForeignKey.NO_ACTION
        )
    ],
    indices = [Index(value = ["quizCategoryId"])])
data class QuizResult(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0, // Consider making it non-nullable if autoGenerate is true
    val dateTaken: String,
    val score: Int,
    val quizCategoryId: Int,
    val userId: String?,
    val questionCount: Int,
    val answeredCorrect: String?,
    val answeredWrong: String?,
    val isWritten: Boolean = false,
    val deleted: Boolean = false,
)