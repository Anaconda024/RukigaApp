package com.example.rukigaapp.services.dao

import androidx.room.*
import com.example.rukigaapp.data.KigaWord
import kotlinx.coroutines.flow.Flow

@Dao
interface KigaWordDao {
    @Update
    suspend fun updateWord(word: KigaWord)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(word: KigaWord)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(words: List<KigaWord>)

    @Query("SELECT COUNT(*) FROM libraries")
    suspend fun getCount(): Int

    @Query("SELECT COUNT(*) FROM libraries WHERE is_deleted = 0")
    suspend fun getActiveCount(): Int

    /**
     * Paginated Query for browsing the dictionary
     * @param limit: How many words to fetch (e.g., 100)
     * @param offset: Where to start (e.g., page 1 starts at 0, page 2 starts at 100)
     */
    @Query("SELECT * FROM libraries WHERE is_deleted = 0 ORDER BY word ASC LIMIT :limit OFFSET :offset")
    suspend fun getWordsPaginated(limit: Int, offset: Int): List<KigaWord>

    /**
     * Search by word only (starts with search query)
     * Returns Flow for real-time UI updates
     */
    @Query("SELECT * FROM libraries WHERE word LIKE :searchQuery || '%' AND is_deleted = 0 ORDER BY word ASC LIMIT 100")
    fun searchByWord(searchQuery: String): Flow<List<KigaWord>>

    /**
     * Search by both word and definition
     * Searches for matches in either the word field (starts with) or definition field (contains)
     * Returns Flow for real-time UI updates as the user types
     */
    @Query("""
        SELECT * FROM libraries 
        WHERE (word LIKE :searchQuery || '%' OR definition LIKE '%' || :searchQuery || '%') 
        AND is_deleted = 0 
        ORDER BY 
            CASE 
                WHEN word LIKE :searchQuery || '%' THEN 0 
                ELSE 1 
            END,
            word ASC 
        LIMIT 100
    """)
    fun searchWordAndDefinition(searchQuery: String): Flow<List<KigaWord>>

    /**
     * Paginated search for both word and definition
     * Use this for pagination when search results exceed 100 items
     * @param searchQuery: The text to search for
     * @param limit: How many results to fetch per page
     * @param offset: Starting position for this page
     */
    @Query("""
        SELECT * FROM libraries 
        WHERE (word LIKE :searchQuery || '%' OR definition LIKE '%' || :searchQuery || '%') 
        AND is_deleted = 0 
        ORDER BY 
            CASE 
                WHEN word LIKE :searchQuery || '%' THEN 0 
                ELSE 1 
            END,
            word ASC 
        LIMIT :limit OFFSET :offset
    """)
    suspend fun searchWordAndDefinitionPaginated(
        searchQuery: String,
        limit: Int,
        offset: Int
    ): List<KigaWord>

    /**
     * Count total search results
     * Used to determine if there are more pages available
     */
    @Query("""
        SELECT COUNT(*) FROM libraries 
        WHERE (word LIKE :searchQuery || '%' OR definition LIKE '%' || :searchQuery || '%') 
        AND is_deleted = 0
    """)
    suspend fun getSearchResultCount(searchQuery: String): Int

    /**
     * Get bookmarked words
     */
    @Query("SELECT * FROM libraries WHERE is_bookmarked = 1 AND is_deleted = 0 ORDER BY word ASC")
    fun getBookmarkedWords(): Flow<List<KigaWord>>

    /**
     * Get bookmarked words paginated
     */
    @Query("SELECT * FROM libraries WHERE is_bookmarked = 1 AND is_deleted = 0 ORDER BY word ASC LIMIT :limit OFFSET :offset")
    suspend fun getBookmarkedWordsPaginated(limit: Int, offset: Int): List<KigaWord>
}