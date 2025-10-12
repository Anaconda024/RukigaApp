package com.example.rukigaapp.services.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Upsert
import com.example.rukigaapp.data.Diction
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow


@Dao
interface DictionDao {

    @Query("SELECT x.*, y.name FROM dictionaries x inner join categories y on x.categoryId = y.id where x.deleted = 0 ORDER BY id ASC")
    fun getDictions(): Flow<List<Diction>>

    @Query("SELECT * FROM dictionaries WHERE categoryId = :categoryId")
    fun getDictionsByCategory(categoryId: Int): Flow<List<Diction>>

    @Query("SELECT * FROM dictionaries WHERE id = :dictionId") // Select all columns for the Diction object
    fun getDiction(dictionId: Int) : Diction?

    @Query(
        "SELECT x.*, y.name AS categoryName FROM dictionaries x " + // Added AS categoryName for clarity
                "INNER JOIN categories y ON x.categoryId = y.id " +
                "WHERE x.deleted = 0 " +
                "AND (:dictionCategoryId IS NULL OR x.categoryId = :dictionCategoryId) " + // Key change here
                "ORDER BY RANDOM() " +
                "LIMIT :numberOfQuestions"
    )
    fun getDictionForQuiz(dictionCategoryId: Int?, numberOfQuestions: Int? = 30): Flow<List<Diction>>

    @Upsert
    suspend fun upsertDiction(diction: Diction)

    @Delete
    suspend fun deleteDiction(diction: Diction)

    @Query("Update dictionaries set deleted = 1 where id = :dictionId")// Select all columns for the Diction object
    fun softDeleteDiction(dictionId: Int)
}