package com.example.catalogue.ui.search

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
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
import com.example.catalogue.ui.common.BackButton
import com.example.catalogue.ui.common.CatalogSearchField
import com.example.catalogue.ui.common.EmptyView
import com.example.catalogue.ui.common.ErrorView
import com.example.catalogue.ui.common.LoadingView
import com.example.catalogue.ui.common.UiState
import com.example.catalogue.ui.common.UiText
import com.example.catalogue.ui.common.productItems
import com.example.catalogue.ui.theme.CatalogGridCells
import com.example.catalogue.ui.theme.Spacing

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    onBack: () -> Unit,
    onProductClick: (String) -> Unit,
    viewModel: SearchViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val query by viewModel.query.collectAsStateWithLifecycle()
    var text by rememberSaveable { mutableStateOf(viewModel.initialQuery) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.title_search)) },
                navigationIcon = {
                    BackButton(onBack = {
                        text = ""
                        onBack()
                    })
                },
            )
        },
    ) { padding ->
        Column(Modifier.padding(padding)) {
            CatalogSearchField(
                query = text,
                onQueryChange = { text = it },
                onSearch = { viewModel.search(text) },
                modifier = Modifier.padding(horizontal = Spacing.md),
            )
            Box(Modifier.weight(1f)) {
                when (val currentState = state) {
                    UiState.Loading -> LoadingView()
                    is UiState.Error -> ErrorView(currentState.message, onRetry = viewModel::retry)
                    is UiState.Success ->
                        if (currentState.data.isEmpty()) {
                            EmptyView(UiText.StringResource(R.string.empty_search_results, listOf(query)))
                        } else {
                            LazyVerticalGrid(
                                columns = CatalogGridCells,
                                contentPadding = PaddingValues(Spacing.md),
                                horizontalArrangement = Arrangement.spacedBy(Spacing.md),
                                verticalArrangement = Arrangement.spacedBy(Spacing.md),
                            ) {
                                productItems(currentState.data) { productId ->
                                    text = ""
                                    onProductClick(productId)
                                }
                            }
                        }
                }
            }
        }
    }
}
