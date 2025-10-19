package com.example.rukigaapp.ui.dictionary


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.rukigaapp.data.Diction
import com.example.rukigaapp.data.DictionState
import com.example.rukigaapp.data.SortType
import com.example.rukigaapp.services.events.DictionEvent
import com.example.rukigaapp.services.DictionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class DictionaryViewModel(
    private val repository: DictionRepository
) : ViewModel() {
    private val _sortType = MutableStateFlow(SortType.English)
    private val _state = MutableStateFlow(DictionState())
    private val _diction = repository.allDiction

    val state = combine(_state, _sortType, _diction, ::mergeState)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DictionState())

    fun onEvent(event: DictionEvent) {
        when (event) {
            is DictionEvent.SelectDiction -> {
                _state.update { it.copy(
                    isAddingDiction = true, // To show the dialog
                    id = event.diction.id,
                    english = event.diction.english,
                    rukiga = event.diction.rukiga,
                    categoryId = event.diction.categoryId
                ) }
            }
            DictionEvent.ShowDialog -> {
                _state.update { it.copy(
                    isAddingDiction = true,
                    // Clear previous data for a new entry
                    id = null,
                    english = "",
                    rukiga = "",
                    categoryId = 0
                ) }
            }
            is DictionEvent.SetDictionEnglish -> {
                _state.value = _state.value.copy(english = event.english)
            }

            is DictionEvent.SetDictionRukiga -> {
                _state.value = _state.value.copy(rukiga = event.rukiga)
            }
            is DictionEvent.SetDictionCategoryId -> {
                _state.value = _state.value.copy(categoryId = event.categoryId)
            }

            is DictionEvent.SaveDiction -> {
                saveDictionToDb()
                _state.update { it.copy(
                    isAddingDiction = false, rukiga = "", english = "", categoryId = 0
                ) }
            }

            is DictionEvent.DeleteDiction -> {
                viewModelScope.launch { repository.delete(event.diction) }
            }

            is DictionEvent.SoftDeleteDiction -> {
                viewModelScope.launch { repository.softDeleteDiction(event.dictionId) }
            }

            is DictionEvent.SortDiction -> {
                _sortType.value = event.sortType
            }

            DictionEvent.HideDialog -> {
                _state.value = _state.value.copy(isAddingDiction = false)
            }

            is DictionEvent.ClearErrorMessage -> {
                _state.value.errorMessage = null
            }

            is DictionEvent.SetDictionId -> {
                _state.value = _state.value.copy(id = event.id)
            }
        }

    }


    private fun saveDictionToDb() {
        val currentState = _state.value
        // Ensure fields are not blank
        if (currentState.english.isBlank() || currentState.rukiga.isBlank()) {
            // Optionally, set an error message in the state
            return
        }

        val diction = Diction(
            id = currentState.id ?: 0, // Use existing ID or 0 for new item
            english = currentState.english,
            rukiga = currentState.rukiga,
            categoryId = currentState.categoryId
        )
        viewModelScope.launch {
            if (diction != null) {
                repository.upsert(diction)
            }
        }
    }

    private fun mergeState(
        state: DictionState,
        sortType: SortType,
        diction: List<Diction>
    ): DictionState {
        return state.copy(
            dictions = diction,
            sortType = sortType
        )
    }
}


