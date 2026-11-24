package com.example.catalogue.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AssistChip
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.catalogue.R
import com.example.catalogue.ui.common.CatalogSearchField
import com.example.catalogue.ui.common.ErrorView
import com.example.catalogue.ui.common.LoadingView
import com.example.catalogue.ui.common.UiState
import com.example.catalogue.ui.common.displayName
import com.example.catalogue.ui.common.productItems
import com.example.catalogue.ui.common.sectionHeader
import com.example.catalogue.ui.theme.CatalogGridCells
import com.example.catalogue.ui.theme.Spacing

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onProductClick: (String) -> Unit,
    onCategoryClick: (String) -> Unit,
    onSearch: (String) -> Unit,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var query by rememberSaveable { mutableStateOf("") }

    Scaffold(topBar = { TopAppBar(title = { Text(stringResource(R.string.app_name)) }) }) { padding ->
        Box(Modifier.padding(padding)) {
            when (val currentState = state) {
                UiState.Loading -> LoadingView()
                is UiState.Error -> ErrorView(currentState.message, onRetry = viewModel::load)
                is UiState.Success -> LazyVerticalGrid(
                    columns = CatalogGridCells,
                    contentPadding = PaddingValues(Spacing.md),
                    horizontalArrangement = Arrangement.spacedBy(Spacing.md),
                    verticalArrangement = Arrangement.spacedBy(Spacing.md),
                ) {
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        CatalogSearchField(
                            query = query,
                            onQueryChange = { query = it },
                            onSearch = { if (query.isNotBlank()) onSearch(query.trim()) },
                        )
                    }
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                            items(currentState.data.categories) { category ->
                                AssistChip(
                                    onClick = { onCategoryClick(category) },
                                    label = { Text(category.displayName()) },
                                )
                            }
                        }
                    }
                    sectionHeader(R.string.section_top_rated)
                    productItems(currentState.data.featured, onProductClick)
                }
            }
        }
    }
}
