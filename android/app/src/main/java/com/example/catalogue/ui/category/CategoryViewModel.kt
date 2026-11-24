package com.example.catalogue.ui.category

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.catalogue.domain.model.CategoryListing
import com.example.catalogue.domain.repository.CatalogRepository
import com.example.catalogue.ui.common.UiState
import com.example.catalogue.ui.common.toUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CategoryViewModel @Inject constructor(
    private val repository: CatalogRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    val category: String = checkNotNull(savedStateHandle.get<String>("category"))

    private val _state = MutableStateFlow<UiState<CategoryListing>>(UiState.Loading)
    val state: StateFlow<UiState<CategoryListing>> = _state.asStateFlow()

    init {
        load()
    }

    fun load() {
        _state.value = UiState.Loading
        viewModelScope.launch { _state.value = repository.categoryListing(category).toUiState() }
    }
}
