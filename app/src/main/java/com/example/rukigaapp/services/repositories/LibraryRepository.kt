package com.example.rukigaapp.services.repositories

import com.example.rukigaapp.data.KigaWord
import com.example.rukigaapp.services.dao.KigaWordDao
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository for managing dictionary/library words.
 *
 * Provides:
 * - Paginated word retrieval (100 items per page)
 * - Search functionality (by word and definition)
 * - Bookmark management
 * - Update operations
 *
 * All operations are coroutine-friendly and use Room's Flow for reactive updates.
 */
@Singleton
class LibraryRepository @Inject constructor(
    private val kigaWordDao: KigaWordDao
) {
    companion object {
        const val PAGE_SIZE = 100
    }

    /**
     * Get total count of active (non-deleted) words in the library
     */
    suspend fun getTotalWordCount(): Int {
        return kigaWordDao.getActiveCount()
    }

    /**
     * Retrieve a paginated list of dictionary words
     *
     * @param page: The page number (0-indexed)
     * @param pageSize: Number of items per page (default: 100)
     * @return List of KigaWord objects for the requested page
     */
    suspend fun getWordsByPage(
        page: Int,
        pageSize: Int = PAGE_SIZE
    ): List<KigaWord> {
        val offset = page * pageSize
        return kigaWordDao.getWordsPaginated(
            limit = pageSize,
            offset = offset
        )
    }

    /**
     * Calculate if there are more pages available
     *
     * @param currentPage: Current page number (0-indexed)
     * @param pageSize: Items per page
     * @return true if there are more pages to load
     */
    suspend fun hasMorePages(
        currentPage: Int,
        pageSize: Int = PAGE_SIZE
    ): Boolean {
        val totalCount = getTotalWordCount()
        val loadedCount = (currentPage + 1) * pageSize
        return loadedCount < totalCount
    }

    /**
     * Search for words by word field only (starts with query)
     * Returns Flow for real-time updates
     * Limited to 100 results
     *
     * @param query: Search text
     * @return Flow of matching words
     */
    fun searchByWord(query: String): Flow<List<KigaWord>> {
        return kigaWordDao.searchByWord(query)
    }

    /**
     * Search for words by both word and definition fields
     * - Word field: matches words that start with the query
     * - Definition field: matches definitions that contain the query
     *
     * Results are ordered with word matches first, then definition matches
     * Returns Flow for real-time updates as user types
     * Limited to 100 results
     *
     * @param query: Search text
     * @return Flow of matching words
     */
    fun searchWordAndDefinition(query: String): Flow<List<KigaWord>> {
        return kigaWordDao.searchWordAndDefinition(query)
    }

    /**
     * Paginated search across word and definition fields
     * Use this when you need pagination for search results
     *
     * @param query: Search text
     * @param page: Page number (0-indexed)
     * @param pageSize: Items per page (default: 100)
     * @return List of matching words for the requested page
     */
    suspend fun searchWordAndDefinitionPaginated(
        query: String,
        page: Int,
        pageSize: Int = PAGE_SIZE
    ): List<KigaWord> {
        val offset = page * pageSize
        return kigaWordDao.searchWordAndDefinitionPaginated(
            searchQuery = query,
            limit = pageSize,
            offset = offset
        )
    }

    /**
     * Get total count of search results
     * Useful for determining if pagination is needed for search
     *
     * @param query: Search text
     * @return Total number of matching results
     */
    suspend fun getSearchResultCount(query: String): Int {
        return kigaWordDao.getSearchResultCount(query)
    }

    /**
     * Check if there are more search results available
     *
     * @param query: Search text
     * @param currentPage: Current page number (0-indexed)
     * @param pageSize: Items per page
     * @return true if there are more search results to load
     */
    suspend fun hasMoreSearchResults(
        query: String,
        currentPage: Int,
        pageSize: Int = PAGE_SIZE
    ): Boolean {
        val totalCount = getSearchResultCount(query)
        val loadedCount = (currentPage + 1) * pageSize
        return loadedCount < totalCount
    }

    /**
     * Update a word (e.g., bookmark status, definition edits)
     *
     * @param word: The KigaWord object with updated fields
     */
    suspend fun updateWord(word: KigaWord) {
        kigaWordDao.updateWord(word)
    }

    /**
     * Insert a new word
     *
     * @param word: The KigaWord object to insert
     */
    suspend fun insertWord(word: KigaWord) {
        kigaWordDao.insert(word)
    }

    /**
     * Insert multiple words (bulk operation)
     *
     * @param words: List of KigaWord objects to insert
     */
    suspend fun insertWords(words: List<KigaWord>) {
        kigaWordDao.insertAll(words)
    }

    /**
     * Toggle bookmark status for a word
     *
     * @param word: The word to bookmark/unbookmark
     * @param categoryId: Optional category ID for organizing bookmarks
     */
    suspend fun toggleBookmark(
        word: KigaWord,
        categoryId: Int = 0
    ) {
        val updatedWord = word.copy(
            isBookmarked = !word.isBookmarked,
            bookmarkCatId = if (!word.isBookmarked) categoryId else 0
        )
        kigaWordDao.updateWord(updatedWord)
    }

    /**
     * Soft delete a word (sets is_deleted flag)
     *
     * @param word: The word to delete
     * @param deletionDate: ISO date string for when the word was deleted
     */
    suspend fun softDeleteWord(
        word: KigaWord,
        deletionDate: String
    ) {
        val updatedWord = word.copy(
            isDeleted = true,
            dateDeleted = deletionDate
        )
        kigaWordDao.updateWord(updatedWord)
    }

    /**
     * Restore a soft-deleted word
     *
     * @param word: The word to restore
     */
    suspend fun restoreWord(word: KigaWord) {
        val updatedWord = word.copy(
            isDeleted = false,
            dateDeleted = null
        )
        kigaWordDao.updateWord(updatedWord)
    }

    /**
     * Get all bookmarked words as Flow
     * Real-time updates when bookmarks change
     */
    fun getBookmarkedWords(): Flow<List<KigaWord>> {
        return kigaWordDao.getBookmarkedWords()
    }

    /**
     * Get bookmarked words with pagination
     *
     * @param page: Page number (0-indexed)
     * @param pageSize: Items per page (default: 100)
     */
    suspend fun getBookmarkedWordsPaginated(
        page: Int,
        pageSize: Int = PAGE_SIZE
    ): List<KigaWord> {
        val offset = page * pageSize
        return kigaWordDao.getBookmarkedWordsPaginated(
            limit = pageSize,
            offset = offset
        )
    }
}