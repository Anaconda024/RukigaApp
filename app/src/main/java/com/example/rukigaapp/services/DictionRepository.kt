package com.example.rukigaapp.services
import androidx.annotation.WorkerThread
import kotlinx.coroutines.flow.Flow

import com.example.rukigaapp.data.Diction
import com.example.rukigaapp.services.dao.DictionDao

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
    suspend fun getdictionById(dictionId: Int): Diction? {
        return dictionDao.getDiction(dictionId)
    }
    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun getdictionByCategory(categoryId: Int): Flow<List<Diction>> {
        return dictionDao.getDictionsByCategory(categoryId)
    }
}