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
                Room.databaseBuilder(context, LearnKigaDatabase::class.java, "rukiga_database2")
                    .fallbackToDestructiveMigration(true)
                    .build()
                    .also { db ->
                        Instance = db
                        CoroutineScope(Dispatchers.IO).launch {
                            val categoryDao = db.categoryDao()
                            if (categoryDao.getCount() == 0) {
                                populateCategories(categoryDao)
                            }
                            val dictionDao = db.dictionDao
                            if (dictionDao.getCount() == 0) {
                                populateDiction(dictionDao)
                            }
                            val quizCategoryDao = db.quizCategoryDao()
                            val quizes = quizCategoryDao.getCount()
                            if (quizes == 0) {
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

        suspend fun populateDiction(dictionDao: DictionDao) {
            val dictionEntries = listOf(
                Diction(id = 1, categoryId = 2, rukiga = "Agandi", english = "How are you?", pronunciation = null, description = null, deleted = false),
                Diction(id = 2, categoryId = 2, rukiga = "Nimarunji", english = "I am fine", pronunciation = null, description = null, deleted = false),
                Diction(id = 4, categoryId = 5, rukiga = "mutale", english = "White", pronunciation = null, description = null, deleted = false),
                Diction(id = 3, categoryId = 3, rukiga = "Emwe", english = "1", pronunciation = null, description = null, deleted = false),
                Diction(id = 5, categoryId = 3, rukiga = "Ibili", english = "2", pronunciation = null, description = null, deleted = false),
                Diction(id = 6, categoryId = 3, rukiga = "Ishatu", english = "3", pronunciation = null, description = null, deleted = false),
                Diction(id = 7, categoryId = 3, rukiga = "Ina", english = "4", pronunciation = null, description = null, deleted = false),
                Diction(id = 7, categoryId = 3, rukiga = "Itano", english = "5", pronunciation = null, description = null, deleted = false),
                Diction(id = 8, categoryId = 3, rukiga = "Mukaga", english = "6", pronunciation = null, description = null, deleted = false),
                Diction(id = 9, categoryId = 3, rukiga = "Mushanjhu", english = "7", pronunciation = null, description = null, deleted = false),
                Diction(id = 10, categoryId = 3, rukiga = "Munana", english = "8", pronunciation = null, description = null, deleted = false),
                Diction(id = 11, categoryId = 3, rukiga = "Mwenda", english = "9", pronunciation = null, description = null, deleted = false),
                Diction(id = 12, categoryId = 3, rukiga = "Ikumi", english = "10", pronunciation = null, description = null, deleted = false),
                Diction(id = 13, categoryId = 1, rukiga = "Ya qweta", english = "Calling you", pronunciation = null, description = null, deleted = false),
                Diction(id = 14, categoryId = 1, rukiga = "Isha", english = "Come", pronunciation = null, description = null, deleted = false),
                Diction(id = 15, categoryId = 1, rukiga = "Webale", english = "Thank you", pronunciation = null, description = null, deleted = false),
                Diction(id = 16, categoryId = 1, rukiga = "Negato", english = "Shoes", pronunciation = null, description = null, deleted = false),
                Diction(id = 17, categoryId = 5, rukiga = "Engito", english = "Hands", pronunciation = null, description = null, deleted = false),
                Diction(id = 18, categoryId = 4, rukiga = "Nenchakale", english = "Tomorrow", pronunciation = null, description = null, deleted = false),
                Diction(id = 19, categoryId = 3, rukiga = "Itano", english = "5", pronunciation = null, description = null, deleted = false)
            )
            dictionDao.insertAll(dictionEntries)
        }
    }
}
