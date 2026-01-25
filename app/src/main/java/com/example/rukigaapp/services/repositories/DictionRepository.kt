package com.example.rukigaapp.services.repositories

import androidx.annotation.WorkerThread
import com.example.rukigaapp.data.CategoryItem
import com.example.rukigaapp.data.Diction
import com.example.rukigaapp.data.enums.Categories
import com.example.rukigaapp.services.dao.DictionDao
import kotlinx.coroutines.flow.Flow

class DictionRepository(private val dictionDao: DictionDao) {
    val allDiction: Flow<List<Diction>> = dictionDao.getDictions()

    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun upsert(diction: Diction) {
        dictionDao.upsertDiction(diction)
    }

    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun delete(diction: Diction) {
        dictionDao.deleteDiction(diction)
    }

    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun softDeleteDiction(diction: Int) {
        dictionDao.softDeleteDiction(diction)
    }

    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun getDictionForQuiz(categoryId: Int?, numberOfQuestions: Int?): Flow<List<Diction>> {
        return dictionDao.getDictionForQuiz(categoryId,numberOfQuestions)
    }

    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun getdictionById(dictionId: Int): Diction? {
        return dictionDao.getDiction(dictionId)
    }
    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun getdictionByCategory(categoryId: Int): Flow<List<Diction>> {
        return dictionDao.getDictionsByCategory(categoryId)
    }

    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun getWordCountByCategory(categoryId: Int): Int {
        return dictionDao.getWordCountByCategory(categoryId)
    }

    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun getAllCategoriesWithCounts(): List<CategoryItem> {
        return Categories.entries.map { category ->
            val count = getWordCountByCategory(category.id)
            CategoryItem(
                id = category.id,
                displayName = category.displayName,
                color = category.color,
                wordCount = count
            )
        }
    }
}