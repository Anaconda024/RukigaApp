package com.example.rukigaapp.services

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.rukigaapp.data.Category
import com.example.rukigaapp.data.Diction
import com.example.rukigaapp.data.QuizCategory
import com.example.rukigaapp.data.QuizResult
import com.example.rukigaapp.data.enums.Categories
import com.example.rukigaapp.data.enums.QuizCategories
import com.example.rukigaapp.services.dao.CategoryDao
import com.example.rukigaapp.services.dao.DictionDao
import com.example.rukigaapp.services.dao.QuizCategoryDao
import com.example.rukigaapp.services.dao.QuizResultDao
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [Diction::class, Category::class, QuizCategory::class, QuizResult::class],
    version = 4,
    exportSchema = true
)
abstract class LearnKigaDatabase: RoomDatabase() {
    abstract val dictionDao: DictionDao
    abstract fun categoryDao(): CategoryDao
    abstract fun quizCategoryDao(): QuizCategoryDao
    abstract fun quizResultDao(): QuizResultDao

    companion object {
        @Volatile
        private var Instance: LearnKigaDatabase? = null

        fun getDatabase(context: Context): LearnKigaDatabase {
            // if the Instance is not null, return it, otherwise create a new database instance.
            return Instance ?: synchronized(this) {
                Room.databaseBuilder(context, LearnKigaDatabase::class.java, "rukiga_database")
                    .fallbackToDestructiveMigration(true)
                    .build()
                    .also { db ->
                        Instance = db
                        CoroutineScope(Dispatchers.IO).launch {
                            val categoryDao = db.categoryDao()
                            if (categoryDao.getCount() == 0) {
                                populateCategories(categoryDao)
                            }
                            val quizCategoryDao = db.quizCategoryDao()
                            if (categoryDao.getCount() == 0) {
                                populateQuizCategories(quizCategoryDao)
                            }
                        }
                    }
            }
        }

        suspend fun populateCategories(categoryDao: CategoryDao) {
            // Your enum values
            val predefinedCategories =
                Categories.entries.map { enumValue ->
                    Category(
                        id = enumValue.id, // Use the ID from the enum
                        name = enumValue.displayName,
                        description = "Default ${enumValue.displayName}" // Optional: provide a default description
                    )
                }
            categoryDao.insertAllCategories(predefinedCategories)
        }

        suspend fun populateQuizCategories(quizCategoryDao: QuizCategoryDao) {
            // Your enum values
            val predefinedCategories =
                QuizCategories.entries.map { enumValue ->
                    QuizCategory(
                        id = enumValue.id, // Use the ID from the enum
                        name = enumValue.displayName,
                        description = "Default ${enumValue.displayName}" // Optional: provide a default description
                    )
                }
            quizCategoryDao.insertAllQuizCategories(predefinedCategories)
        }
    }
}
