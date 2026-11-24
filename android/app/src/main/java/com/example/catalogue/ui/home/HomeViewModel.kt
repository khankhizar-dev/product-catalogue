package com.example.catalogue.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.catalogue.domain.model.Product
import com.example.catalogue.domain.repository.CatalogRepository
import com.example.catalogue.ui.common.UiState
import com.example.catalogue.ui.common.toUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeContent(val featured: List<Product>, val categories: List<String>)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: CatalogRepository,
) : ViewModel() {

    private val _state = MutableStateFlow<UiState<HomeContent>>(UiState.Loading)
    val state: StateFlow<UiState<HomeContent>> = _state.asStateFlow()

    init {
        load()
    }

    fun load() {
        _state.value = UiState.Loading
        viewModelScope.launch {
            _state.value = coroutineScope {
                val featured = async { repository.featured() }
                val categories = async { repository.categories() }
                val f = featured.await()
                val c = categories.await()
                val failure = f.exceptionOrNull() ?: c.exceptionOrNull()
                if (failure != null) {
                    Result.failure<HomeContent>(failure).toUiState()
                } else {
                    UiState.Success(HomeContent(f.getOrThrow(), c.getOrThrow()))
                }
            }
        }
    }
}
