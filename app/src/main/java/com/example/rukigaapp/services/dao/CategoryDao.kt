package com.example.rukigaapp.services.dao // Or your DAO package

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.rukigaapp.data.Category
import kotlinx.coroutines.flow.Flow

@Dao
interface CategoryDao {
    @Insert
    suspend fun insertCategory(category: Category)

    @Insert(onConflict = OnConflictStrategy.REPLACE) // Or IGNORE
    suspend fun insertAllCategories(categories: List<Category>)

    @Query("SELECT * FROM categories ORDER BY name ASC") // Assuming your table is named 'categories'
    fun getAllCategories(): Flow<List<Category>>

    @Query("SELECT * FROM categories WHERE id = :id")
    suspend fun getCategoryById(id: Int): Category?

    @Query("SELECT COUNT(*) FROM categories")
    suspend fun getCount(): Int

    // Add other necessary query methods (findById, update, delete, etc.)
}