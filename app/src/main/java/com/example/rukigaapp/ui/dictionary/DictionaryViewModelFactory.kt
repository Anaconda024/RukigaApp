package com.example.rukigaapp.ui.dictionary

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.rukigaapp.services.DictionRepository

class DictionaryViewModelFactory(
    private val repository: DictionRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DictionaryViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return DictionaryViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
