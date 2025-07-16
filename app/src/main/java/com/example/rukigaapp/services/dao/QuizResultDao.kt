package com.example.rukigaapp.services.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Upsert
import com.example.rukigaapp.data.QuizResult
import kotlinx.coroutines.flow.Flow

@Dao
interface QuizResultDao {

    @Query("SELECT x.*, y.name FROM quizResults x inner join quizCategories y on x.quizCategoryId = y.id where x.deleted = 0 ORDER BY id ASC")
     fun getQuizResults(): Flow<List<QuizResult>> 

    @Query("SELECT * FROM quizResults WHERE quizCategoryId = :categoryId")
     fun getQuizResultsByCategory(categoryId: Int): Flow<List<QuizResult>>

    @Query("SELECT * FROM quizResults WHERE id = :quizResultId") // Select all columns for the QuizResult object
    fun getQuizResult(quizResultId: Int) : QuizResult?

    @Upsert
    suspend fun upsertQuizResult(quizResult: QuizResult)

    @Delete
    suspend fun deleteQuizResult(quizResult: QuizResult)

    @Query("Update quizResults set deleted = 1 where id = :quizResultId")// Select all columns for the QuizResult object
    fun softDeleteQuizResult(quizResultId: Int)
}