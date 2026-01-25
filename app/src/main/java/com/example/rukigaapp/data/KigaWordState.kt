package com.example.rukigaapp.data

data class KigaWordState (
    val words: List<KigaWord> = emptyList(),

    // Pagination
    val currentPage: Int = 0,
    val hasMorePages: Boolean = true,
    val isLoadingMore: Boolean = false,

    // Search
    val searchQuery: String = "",
    val isSearchActive: Boolean = false,
    val searchResultCount: Int = 0,

    // Loading states
    val isInitialLoading: Boolean = false,
    val isRefreshing: Boolean = false,

    // Error handling
    val errorMessage: String? = null,

    // Selection/Detail view (optional)
    val selectedWord: KigaWord? = null,
    val isShowingDetail: Boolean = false,

    // Statistics
    val totalWordCount: Int = 0
)