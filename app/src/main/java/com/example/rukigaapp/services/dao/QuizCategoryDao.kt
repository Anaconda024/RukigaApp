package com.example.rukigaapp.services.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.rukigaapp.data.QuizCategory
import kotlinx.coroutines.flow.Flow

@Dao
interface QuizCategoryDao {
    @Insert
    suspend fun insertQuizCategory(quizCategory: QuizCategory)

    @Insert(onConflict = OnConflictStrategy.REPLACE) // Or IGNORE
    suspend fun insertAllQuizCategories(categories: List<QuizCategory>)

    @Query("SELECT * FROM categories ORDER BY name ASC") // Assuming your table is named 'categories'
    fun getAllQuizCategories(): Flow<List<QuizCategory>>

    @Query("SELECT * FROM categories WHERE id = :id")
    suspend fun getQuizCategoryById(id: Int): QuizCategory?

    @Query("SELECT COUNT(*) FROM categories")
    suspend fun getCount(): Int

    // Add other necessary query methods (findById, update, delete, etc.)
}