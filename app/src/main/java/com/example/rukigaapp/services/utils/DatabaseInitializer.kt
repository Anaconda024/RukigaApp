package com.example.rukigaapp.services.utils

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.example.rukigaapp.data.Category
import com.example.rukigaapp.data.Diction
import com.example.rukigaapp.data.QuizCategory
import com.example.rukigaapp.data.enums.Categories
import com.example.rukigaapp.data.enums.QuizCategories
import com.example.rukigaapp.services.LearnKigaDatabase
import com.example.rukigaapp.services.dao.CategoryDao
import com.example.rukigaapp.services.dao.KigaWordDao
import com.example.rukigaapp.services.dao.QuizCategoryDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Handles all first-time initialization of the database,
 * including importing the dictionary and populating other tables.
 */
class DatabaseInitializer(
    private val context: Context,
    private val database: LearnKigaDatabase
) {

    companion object {
        private const val TAG = "DatabaseInitializer"
        private const val PREFS_NAME = "rukiga_app_prefs"
        private const val KEY_DB_INITIALIZED = "database_initialized"
        private const val KEY_DICTIONARY_IMPORTED = "dictionary_imported"
    }

    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    /**
     * Initialize the database if it hasn't been initialized yet.
     * This is the main entry point called from the database companion object.
     */
    suspend fun initializeIfNeeded() = withContext(Dispatchers.IO) {
        if (isDatabaseInitialized()) {
            Log.d(TAG, "Database already initialized, skipping...")
            return@withContext
        }

        Log.d(TAG, "Starting database initialization...")

        try {
            // Step 1: Populate categories
            populateCategoriesIfNeeded()

            // Step 2: Populate quiz categories
            populateQuizCategoriesIfNeeded()

            // Step 3: Populate sample diction entries
            populateDictionIfNeeded()

            // Step 4: Import dictionary from assets
            importDictionaryIfNeeded()

            // Mark as initialized
            markAsInitialized()

            Log.d(TAG, "Database initialization completed successfully")

        } catch (e: Exception) {
            Log.e(TAG, "Error during database initialization", e)
            throw e
        }
    }

    /**
     * Check if database has been initialized
     */
    private fun isDatabaseInitialized(): Boolean {
        return prefs.getBoolean(KEY_DB_INITIALIZED, false)
    }

    /**
     * Mark database as initialized
     */
    private fun markAsInitialized() {
        prefs.edit().putBoolean(KEY_DB_INITIALIZED, true).apply()
    }

    /**
     * Populate categories table if empty
     */
    private suspend fun populateCategoriesIfNeeded() {
        val categoryDao = database.categoryDao()

        if (categoryDao.getCount() > 0) {
            Log.d(TAG, "Categories already populated, skipping...")
            return
        }

        Log.d(TAG, "Populating categories...")

        val predefinedCategories = Categories.entries.map { enumValue ->
            Category(
                id = enumValue.id,
                name = enumValue.displayName,
                description = "Default ${enumValue.displayName}"
            )
        }

        categoryDao.insertAllCategories(predefinedCategories)
        Log.d(TAG, "Populated ${predefinedCategories.size} categories")
    }

    /**
     * Populate quiz categories table if empty
     */
    private suspend fun populateQuizCategoriesIfNeeded() {
        val quizCategoryDao = database.quizCategoryDao()

        if (quizCategoryDao.getCount() > 0) {
            Log.d(TAG, "Quiz categories already populated, skipping...")
            return
        }

        Log.d(TAG, "Populating quiz categories...")

        val predefinedCategories = QuizCategories.entries.map { enumValue ->
            QuizCategory(
                id = enumValue.id,
                name = enumValue.displayName,
                description = "Default ${enumValue.displayName}"
            )
        }

        quizCategoryDao.insertAllQuizCategories(predefinedCategories)
        Log.d(TAG, "Populated ${predefinedCategories.size} quiz categories")
    }

    /**
     * Populate sample diction entries if empty
     */
    private suspend fun populateDictionIfNeeded() {
        val dictionDao = database.dictionDao

        if (dictionDao.getCount() > 0) {
            Log.d(TAG, "Diction entries already populated, skipping...")
            return
        }

        Log.d(TAG, "Populating sample diction entries...")

        val dictionEntries = listOf(
            Diction(id = 1, categoryId = 2, rukiga = "Agandi", english = "How are you?", pronunciation = null, description = null, deleted = false),
            Diction(id = 2, categoryId = 2, rukiga = "Nimarunji", english = "I am fine", pronunciation = null, description = null, deleted = false),
            Diction(id = 4, categoryId = 5, rukiga = "mutale", english = "White", pronunciation = null, description = null, deleted = false),
            Diction(id = 3, categoryId = 3, rukiga = "Emwe", english = "1", pronunciation = null, description = null, deleted = false),
            Diction(id = 5, categoryId = 3, rukiga = "Ibili", english = "2", pronunciation = null, description = null, deleted = false),
            Diction(id = 6, categoryId = 3, rukiga = "Ishatu", english = "3", pronunciation = null, description = null, deleted = false),
            Diction(id = 7, categoryId = 3, rukiga = "Ina", english = "4", pronunciation = null, description = null, deleted = false),
            Diction(id = 8, categoryId = 3, rukiga = "Itano", english = "5", pronunciation = null, description = null, deleted = false),
            Diction(id = 9, categoryId = 3, rukiga = "Mukaga", english = "6", pronunciation = null, description = null, deleted = false),
            Diction(id = 10, categoryId = 3, rukiga = "Mushanjhu", english = "7", pronunciation = null, description = null, deleted = false),
            Diction(id = 11, categoryId = 3, rukiga = "Munana", english = "8", pronunciation = null, description = null, deleted = false),
            Diction(id = 12, categoryId = 3, rukiga = "Mwenda", english = "9", pronunciation = null, description = null, deleted = false),
            Diction(id = 13, categoryId = 3, rukiga = "Ikumi", english = "10", pronunciation = null, description = null, deleted = false),
            Diction(id = 14, categoryId = 1, rukiga = "Ya qweta", english = "Calling you", pronunciation = null, description = null, deleted = false),
            Diction(id = 15, categoryId = 1, rukiga = "Isha", english = "Come", pronunciation = null, description = null, deleted = false),
            Diction(id = 16, categoryId = 1, rukiga = "Webale", english = "Thank you", pronunciation = null, description = null, deleted = false),
            Diction(id = 17, categoryId = 1, rukiga = "Negato", english = "Shoes", pronunciation = null, description = null, deleted = false),
            Diction(id = 18, categoryId = 5, rukiga = "Engito", english = "Hands", pronunciation = null, description = null, deleted = false),
            Diction(id = 19, categoryId = 4, rukiga = "Nenchakale", english = "Tomorrow", pronunciation = null, description = null, deleted = false),
            Diction(id = 20, categoryId = 3, rukiga = "Itano", english = "5", pronunciation = null, description = null, deleted = false)
        )

        dictionDao.insertAll(dictionEntries)
        Log.d(TAG, "Populated ${dictionEntries.size} sample diction entries")
    }

    /**
     * Import dictionary from assets if not already imported
     */
    private suspend fun importDictionaryIfNeeded() {
        // Check if already imported (via SharedPreferences for reliability)
        if (prefs.getBoolean(KEY_DICTIONARY_IMPORTED, false)) {
            Log.d(TAG, "Dictionary already imported, skipping...")
            return
        }

        // Double-check by counting records
        val kigaWordDao = database.kigaWordDao()
        if (kigaWordDao.getCount() > 0) {
            Log.d(TAG, "Dictionary table has records, skipping import...")
            prefs.edit().putBoolean(KEY_DICTIONARY_IMPORTED, true).apply()
            return
        }

        Log.d(TAG, "Importing dictionary from assets...")

        val wordsImported = DatabaseImporter.importDictionaryFromAssets(
            context = context,
            kigaWordDao = kigaWordDao
        )

        if (wordsImported > 0) {
            prefs.edit().putBoolean(KEY_DICTIONARY_IMPORTED, true).apply()
            Log.d(TAG, "Successfully imported $wordsImported words")
        } else {
            Log.e(TAG, "Failed to import dictionary")
            throw Exception("Dictionary import failed")
        }
    }

    /**
     * Reset the initialization state (useful for testing)
     */
    fun resetInitializationState() {
        prefs.edit()
            .putBoolean(KEY_DB_INITIALIZED, false)
            .putBoolean(KEY_DICTIONARY_IMPORTED, false)
            .apply()
        Log.d(TAG, "Initialization state reset")
    }
}