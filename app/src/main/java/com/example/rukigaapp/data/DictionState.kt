package com.example.rukigaapp.data

data class DictionState(
    val dictions: List<Diction> = emptyList(),
    val id: Int? = null,
    val rukiga: String = "",
    val english: String = "",
    val categoryId: Int =  0,
    val isAddingDiction: Boolean = false,
    val sortType: SortType = SortType.English,
    var errorMessage: String? = null
)
