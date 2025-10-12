package com.example.rukigaapp.services

import androidx.annotation.WorkerThread
import com.example.rukigaapp.data.QuizResult
import com.example.rukigaapp.services.dao.QuizResultDao
import kotlinx.coroutines.flow.Flow

class QuizResultRepository(private val quizResultDao: QuizResultDao) {
    val allQuizResult: Flow<List<QuizResult>> = quizResultDao.getQuizResults()

    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun upsert(quizResult: QuizResult) {
        quizResultDao.upsertQuizResult(quizResult)
    }

    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun softDeleteQuizResult(quizResult: Int) {
        quizResultDao.softDeleteQuizResult(quizResult)
    }

    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun getquizResultById(quizResultId: Int): QuizResult? {
        return quizResultDao.getQuizResult(quizResultId)
    }
}