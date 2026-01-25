package com.example.rukigaapp.services

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.rukigaapp.data.Category
import com.example.rukigaapp.data.Diction
import com.example.rukigaapp.data.KigaWord
import com.example.rukigaapp.data.QuizCategory
import com.example.rukigaapp.data.QuizResult
import com.example.rukigaapp.services.dao.CategoryDao
import com.example.rukigaapp.services.dao.DictionDao
import com.example.rukigaapp.services.dao.KigaWordDao
import com.example.rukigaapp.services.dao.QuizCategoryDao
import com.example.rukigaapp.services.dao.QuizResultDao
import com.example.rukigaapp.services.utils.DatabaseInitializer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * Main Room database for the Rukiga learning app.
 *
 * This database includes:
 * - KigaWord: The main dictionary (imported from assets)
 * - Diction: Sample/custom dictionary entries
 * - Category: Learning categories
 * - QuizCategory: Quiz categories
 * - QuizResult: User quiz results
 */
@Database(
    entities = [
        KigaWord::class,
        Diction::class,
        Category::class,
        QuizCategory::class,
        QuizResult::class
    ],
    version = 5, // Incremented to accommodate new table
    exportSchema = true
)
abstract class LearnKigaDatabase : RoomDatabase() {

    // DAOs
    abstract fun kigaWordDao(): KigaWordDao
    abstract val dictionDao: DictionDao
    abstract fun categoryDao(): CategoryDao
    abstract fun quizCategoryDao(): QuizCategoryDao
    abstract fun quizResultDao(): QuizResultDao

    companion object {
        @Volatile
        private var INSTANCE: LearnKigaDatabase? = null

        private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

        /**
         * Gets or creates the database instance.
         * Initializes all tables on first creation.
         */
        fun getDatabase(context: Context): LearnKigaDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = buildDatabase(context)
                INSTANCE = instance

                // Initialize database asynchronously
                applicationScope.launch {
                    val initializer = DatabaseInitializer(
                        context = context.applicationContext,
                        database = instance
                    )
                    initializer.initializeIfNeeded()
                }

                instance
            }
        }

        /**
         * Builds the Room database with proper configuration
         */
        private fun buildDatabase(context: Context): LearnKigaDatabase {
            return Room.databaseBuilder(
                context.applicationContext,
                LearnKigaDatabase::class.java,
                "rukiga_database3" // Changed name to trigger migration
            )
                .fallbackToDestructiveMigration() // For development; implement proper migrations for production
                .build()
        }

        /**
         * For testing: destroys the database instance
         */
        @Synchronized
        fun destroyInstance() {
            INSTANCE?.close()
            INSTANCE = null
        }
    }
}