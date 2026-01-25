package com.example.rukigaapp.ui.library

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.rukigaapp.data.KigaWord
import com.example.rukigaapp.data.KigaWordState
import com.example.rukigaapp.services.repositories.LibraryRepository
import com.example.rukigaapp.services.events.KigaWordEvent
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

/**
 * ViewModel for the Library/Dictionary screen
 *
 * Manages:
 * - Paginated word loading (100 items per page)
 * - Search functionality (word + definition)
 * - Bookmark toggling
 * - Word selection and navigation
 */
@OptIn(FlowPreview::class)
class LibraryViewModel(
    private val repository: LibraryRepository
) : ViewModel() {

    private val _state = MutableStateFlow(KigaWordState())
    val state: StateFlow<KigaWordState> = _state.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    private var searchJob: Job? = null
    private var loadMoreJob: Job? = null

    init {
        loadInitialWords()
        observeSearchQuery()
    }

    /**
     * Handle events from the UI
     */
    fun onEvent(event: KigaWordEvent) {
        when (event) {
            is KigaWordEvent.OnWordClicked -> {
                selectWord(event.word)
            }

            is KigaWordEvent.ToggleBookmark -> {
                toggleBookmark(event.word)
            }

            is KigaWordEvent.LoadMoreWords -> {
                loadMoreWords()
            }

            is KigaWordEvent.SearchWords -> {
                updateSearchQuery(event.query)
            }

            is KigaWordEvent.ClearSearch -> {
                clearSearch()
            }

            is KigaWordEvent.DeleteWord -> {
                deleteWord(event.word)
            }

            is KigaWordEvent.RestoreWord -> {
                restoreWord(event.word)
            }

            // Legacy events (if needed)
            is KigaWordEvent.SaveKigaWord -> {
                // Handle if needed
            }

            is KigaWordEvent.SetDictionKigaWord -> {
                // Handle if needed
            }

            is KigaWordEvent.BookmarkWithCategory -> TODO()
            KigaWordEvent.ClearError -> TODO()
            KigaWordEvent.ClearSelectedWord -> TODO()
            is KigaWordEvent.InsertWord -> TODO()
            KigaWordEvent.RefreshWords -> TODO()
            is KigaWordEvent.RemoveBookmark -> TODO()
            is KigaWordEvent.SelectWord -> TODO()
            KigaWordEvent.ShowAllWords -> TODO()
            KigaWordEvent.ShowBookmarksOnly -> TODO()
            is KigaWordEvent.UpdateWord -> TODO()
        }
    }

    /**
     * Load initial batch of words
     */
    private fun loadInitialWords() {
        viewModelScope.launch {
            _state.update { it.copy(isInitialLoading = true, errorMessage = null) }

            try {
                val totalCount = repository.getTotalWordCount()
                val words = repository.getWordsByPage(page = 0)
                val hasMore = repository.hasMorePages(currentPage = 0)

                _state.update {
                    it.copy(
                        words = words,
                        currentPage = 0,
                        hasMorePages = hasMore,
                        totalWordCount = totalCount,
                        isInitialLoading = false,
                        errorMessage = null
                    )
                }
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        isInitialLoading = false,
                        errorMessage = "Failed to load words: ${e.localizedMessage}"
                    )
                }
            }
        }
    }

    /**
     * Load more words for pagination (infinite scroll)
     */
    private fun loadMoreWords() {
        // Prevent multiple simultaneous loads
        if (_state.value.isLoadingMore || !_state.value.hasMorePages) {
            return
        }

        // Cancel previous job if still running
        loadMoreJob?.cancel()

        loadMoreJob = viewModelScope.launch {
            _state.update { it.copy(isLoadingMore = true, errorMessage = null) }

            try {
                val nextPage = _state.value.currentPage + 1
                val newWords = if (_state.value.isSearchActive) {
                    // Load more search results
                    repository.searchWordAndDefinitionPaginated(
                        query = _state.value.searchQuery,
                        page = nextPage
                    )
                } else {
                    // Load more regular words
                    repository.getWordsByPage(page = nextPage)
                }

                val hasMore = if (_state.value.isSearchActive) {
                    repository.hasMoreSearchResults(
                        query = _state.value.searchQuery,
                        currentPage = nextPage
                    )
                } else {
                    repository.hasMorePages(currentPage = nextPage)
                }

                _state.update {
                    it.copy(
                        words = it.words + newWords, // Append new words
                        currentPage = nextPage,
                        hasMorePages = hasMore,
                        isLoadingMore = false,
                        errorMessage = null
                    )
                }
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        isLoadingMore = false,
                        errorMessage = "Failed to load more words: ${e.localizedMessage}"
                    )
                }
            }
        }
    }

    /**
     * Observe search query changes with debounce
     */
    private fun observeSearchQuery() {
        viewModelScope.launch {
            _searchQuery
                .debounce(300) // Wait 300ms after user stops typing
                .distinctUntilChanged()
                .collectLatest { query ->
                    performSearch(query)
                }
        }
    }

    /**
     * Update search query (will trigger debounced search)
     */
    private fun updateSearchQuery(query: String) {
        _searchQuery.value = query
        _state.update { it.copy(searchQuery = query) }
    }

    /**
     * Perform the actual search
     */
    private fun performSearch(query: String) {
        // Cancel previous search job
        searchJob?.cancel()

        if (query.isBlank()) {
            // Empty query - reload initial words
            clearSearch()
            return
        }

        searchJob = viewModelScope.launch {
            _state.update {
                it.copy(
                    isInitialLoading = true,
                    isSearchActive = true,
                    errorMessage = null
                )
            }

            try {
                // Get search results count
                val resultCount = repository.getSearchResultCount(query)

                // Collect search results as Flow
                repository.searchWordAndDefinition(query)
                    .collectLatest { searchResults ->
                        val hasMore = repository.hasMoreSearchResults(
                            query = query,
                            currentPage = 0
                        )

                        _state.update {
                            it.copy(
                                words = searchResults,
                                currentPage = 0,
                                hasMorePages = hasMore,
                                searchResultCount = resultCount,
                                isSearchActive = true,
                                isInitialLoading = false,
                                errorMessage = null
                            )
                        }
                    }
            } catch (e: Exception) {
                _state.update {
                    it.copy(
                        isInitialLoading = false,
                        errorMessage = "Search failed: ${e.localizedMessage}"
                    )
                }
            }
        }
    }

    /**
     * Clear search and reload initial words
     */
    private fun clearSearch() {
        searchJob?.cancel()
        _searchQuery.value = ""
        _state.update {
            it.copy(
                searchQuery = "",
                isSearchActive = false,
                searchResultCount = 0
            )
        }
        loadInitialWords()
    }

    /**
     * Toggle bookmark status for a word
     */
    private fun toggleBookmark(word: KigaWord) {
        viewModelScope.launch {
            try {
                repository.toggleBookmark(word)

                // Update the word in the current list
                _state.update { currentState ->
                    currentState.copy(
                        words = currentState.words.map { w ->
                            if (w.id == word.id) {
                                w.copy(isBookmarked = !w.isBookmarked)
                            } else {
                                w
                            }
                        }
                    )
                }
            } catch (e: Exception) {
                _state.update {
                    it.copy(errorMessage = "Failed to update bookmark: ${e.localizedMessage}")
                }
            }
        }
    }

    /**
     * Select a word (for detail view or navigation)
     */
    private fun selectWord(word: KigaWord) {
        _state.update {
            it.copy(
                selectedWord = word,
                isShowingDetail = true
            )
        }
    }

    /**
     * Clear selected word
     */
    fun clearSelectedWord() {
        _state.update {
            it.copy(
                selectedWord = null,
                isShowingDetail = false
            )
        }
    }

    /**
     * Soft delete a word
     */
    private fun deleteWord(word: KigaWord) {
        viewModelScope.launch {
            try {
                val currentDate = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                    .format(Date())

                repository.softDeleteWord(word, currentDate)

                // Remove from current list
                _state.update { currentState ->
                    currentState.copy(
                        words = currentState.words.filter { it.id != word.id }
                    )
                }
            } catch (e: Exception) {
                _state.update {
                    it.copy(errorMessage = "Failed to delete word: ${e.localizedMessage}")
                }
            }
        }
    }

    /**
     * Restore a soft-deleted word
     */
    private fun restoreWord(word: KigaWord) {
        viewModelScope.launch {
            try {
                repository.restoreWord(word)
                // Optionally reload words or update the list
            } catch (e: Exception) {
                _state.update {
                    it.copy(errorMessage = "Failed to restore word: ${e.localizedMessage}")
                }
            }
        }
    }

    /**
     * Refresh the word list
     */
    fun refresh() {
        if (_state.value.isSearchActive) {
            performSearch(_state.value.searchQuery)
        } else {
            loadInitialWords()
        }
    }

    /**
     * Clear error message
     */
    fun clearError() {
        _state.update { it.copy(errorMessage = null) }
    }
}