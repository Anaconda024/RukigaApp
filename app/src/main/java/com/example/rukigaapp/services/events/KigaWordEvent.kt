package com.example.rukigaapp.services.events

import com.example.rukigaapp.data.KigaWord

/**
 * Sealed interface defining all possible events/actions for KigaWord/Library functionality
 *
 * Events are organized by feature:
 * - Legacy events (from original implementation)
 * - Library browsing events
 * - Search events
 * - Bookmark events
 * - Word management events
 */
sealed interface KigaWordEvent {

    // ============================================================
    // LEGACY EVENTS (Original implementation)
    // ============================================================

    /**
     * Save a KigaWord to the database
     */
    object SaveKigaWord : KigaWordEvent

    /**
     * Set dictionary/bookmark status for a KigaWord
     * @param bookmark: Whether to bookmark the word
     */
    data class SetDictionKigaWord(val bookmark: Boolean) : KigaWordEvent


    // ============================================================
    // LIBRARY BROWSING EVENTS
    // ============================================================

    /**
     * User clicked on a word item (for navigation or detail view)
     * @param word: The KigaWord that was clicked
     */
    data class OnWordClicked(val word: KigaWord) : KigaWordEvent

    /**
     * Load more words for pagination (infinite scroll)
     * @param page: The page number to load (optional, ViewModel tracks current page)
     */
    data class LoadMoreWords(val page: Int = 0) : KigaWordEvent

    /**
     * Refresh the current word list
     */
    object RefreshWords : KigaWordEvent


    // ============================================================
    // SEARCH EVENTS
    // ============================================================

    /**
     * Search for words by word text or definition
     * @param query: The search text
     */
    data class SearchWords(val query: String) : KigaWordEvent

    /**
     * Clear the current search and return to browse mode
     */
    object ClearSearch : KigaWordEvent


    // ============================================================
    // BOOKMARK EVENTS
    // ============================================================

    /**
     * Toggle bookmark status for a word
     * @param word: The word to bookmark/unbookmark
     */
    data class ToggleBookmark(val word: KigaWord) : KigaWordEvent

    /**
     * Bookmark a word with a specific category
     * @param word: The word to bookmark
     * @param categoryId: The bookmark category ID
     */
    data class BookmarkWithCategory(val word: KigaWord, val categoryId: Int) : KigaWordEvent

    /**
     * Remove bookmark from a word
     * @param word: The word to remove bookmark from
     */
    data class RemoveBookmark(val word: KigaWord) : KigaWordEvent

    /**
     * Show only bookmarked words
     */
    object ShowBookmarksOnly : KigaWordEvent

    /**
     * Show all words (including non-bookmarked)
     */
    object ShowAllWords : KigaWordEvent


    // ============================================================
    // WORD MANAGEMENT EVENTS
    // ============================================================

    /**
     * Soft delete a word (sets is_deleted flag)
     * @param word: The word to delete
     */
    data class DeleteWord(val word: KigaWord) : KigaWordEvent

    /**
     * Restore a soft-deleted word
     * @param word: The word to restore
     */
    data class RestoreWord(val word: KigaWord) : KigaWordEvent

    /**
     * Update a word's details
     * @param word: The updated word object
     */
    data class UpdateWord(val word: KigaWord) : KigaWordEvent

    /**
     * Insert a new word into the dictionary
     * @param word: The new word to add
     */
    data class InsertWord(val word: KigaWord) : KigaWordEvent


    // ============================================================
    // UI STATE EVENTS
    // ============================================================

    /**
     * Clear the currently selected word (close detail view)
     */
    object ClearSelectedWord : KigaWordEvent

    /**
     * Clear any error messages
     */
    object ClearError : KigaWordEvent

    /**
     * Select a word for detail viewing
     * @param word: The word to view in detail
     */
    data class SelectWord(val word: KigaWord) : KigaWordEvent
}