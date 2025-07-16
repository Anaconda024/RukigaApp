package com.example.rukigaapp.services.events

import com.example.rukigaapp.data.Diction
import com.example.rukigaapp.data.SortType

sealed interface DictionEvent {
    object SaveDiction: DictionEvent
    data class SetDictionRukiga(val rukiga: String): DictionEvent
    data class SetDictionEnglish(val english: String): DictionEvent
    data class SetDictionCategoryId(val categoryId: Int): DictionEvent
    object ShowDialog: DictionEvent
    object HideDialog: DictionEvent
    data class SortDiction(val sortType: SortType): DictionEvent
    data class DeleteDiction(val diction: Diction): DictionEvent
    data class SoftDeleteDiction(val dictionId: Int): DictionEvent
    data class ClearErrorMessage(val errorMessage: String?): DictionEvent

}