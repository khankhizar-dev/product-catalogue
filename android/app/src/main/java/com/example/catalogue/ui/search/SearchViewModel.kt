package com.example.catalogue.ui.search

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.catalogue.domain.model.Product
import com.example.catalogue.domain.repository.CatalogRepository
import com.example.catalogue.ui.common.UiState
import com.example.catalogue.ui.common.toUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val repository: CatalogRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    val initialQuery: String = savedStateHandle.get<String>("query").orEmpty()

    private val _query = MutableStateFlow(initialQuery.trim())
    val query: StateFlow<String> = _query.asStateFlow()

    private val _state = MutableStateFlow<UiState<List<Product>>>(UiState.Loading)
    val state: StateFlow<UiState<List<Product>>> = _state.asStateFlow()

    private var searchJob: Job? = null

    init {
        if (initialQuery.isBlank()) {
            _state.value = UiState.Success(emptyList())
        } else {
            search(initialQuery)
        }
    }

    fun search(text: String) {
        val trimmed = text.trim()
        if (trimmed.isEmpty()) return
        _query.value = trimmed
        _state.value = UiState.Loading
        searchJob?.cancel() // a newer search replaces an in-flight one
        searchJob = viewModelScope.launch { _state.value = repository.search(trimmed).toUiState() }
    }

    fun retry() = search(_query.value)
}
