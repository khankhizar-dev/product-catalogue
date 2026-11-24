package com.example.catalogue.ui.category

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.catalogue.R
import com.example.catalogue.domain.model.CategoryListing
import com.example.catalogue.domain.model.Product
import com.example.catalogue.ui.common.BackButton
import com.example.catalogue.ui.common.ErrorView
import com.example.catalogue.ui.common.LoadingView
import com.example.catalogue.ui.common.UiState
import com.example.catalogue.ui.common.displayName
import com.example.catalogue.ui.common.productItems
import com.example.catalogue.ui.common.sectionHeader
import com.example.catalogue.ui.theme.CatalogGridCells
import com.example.catalogue.ui.theme.CatalogTheme
import com.example.catalogue.ui.theme.Spacing

@Composable
fun CategoryScreen(
    onBack: () -> Unit,
    onProductClick: (String) -> Unit,
    viewModel: CategoryViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val title = viewModel.category.displayName()

    CategoryScreenContent(
        state = state,
        title = title,
        onBack = onBack,
        onProductClick = onProductClick,
        onRetry = viewModel::load,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryScreenContent(
    state: UiState<CategoryListing>,
    title: String,
    onBack: () -> Unit,
    onProductClick: (String) -> Unit,
    onRetry: () -> Unit,
) {
    Scaffold(
        topBar = { TopAppBar(title = { Text(title) }, navigationIcon = { BackButton(onBack) }) },
    ) { padding ->
        Box(Modifier.padding(padding)) {
            when (val currentState = state) {
                UiState.Loading -> LoadingView()
                is UiState.Error -> ErrorView(currentState.message, onRetry = onRetry)
                is UiState.Success -> LazyVerticalGrid(
                    columns = CatalogGridCells,
                    contentPadding = PaddingValues(Spacing.md),
                    horizontalArrangement = Arrangement.spacedBy(Spacing.md),
                    verticalArrangement = Arrangement.spacedBy(Spacing.md),
                ) {
                    sectionHeader(R.string.section_top_rated)
                    productItems(currentState.data.topRated, onProductClick)
                    if (currentState.data.others.isNotEmpty()) {
                        sectionHeader(R.string.section_more_in_category, title)
                        productItems(currentState.data.others, onProductClick)
                    }
                }
            }
        }
    }
}

@Preview(name = "Category Screen - Light", showBackground = true)
@Preview(name = "Category Screen - Dark", uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
fun CategoryScreenPreview() {
    CatalogTheme {
        CategoryScreenContent(
            state = UiState.Success(
                CategoryListing(
                    category = "jewelery",
                    topRated = listOf(
                        Product("1", "Gold Ring", "Nice ring", "jewelery", 99.99, "", 4.8, 45),
                    ),
                    others = listOf(
                        Product("2", "Silver Bracelet", "Nice bracelet", "jewelery", 49.99, "", 4.5, 30),
                    ),
                )
            ),
            title = "Jewelery",
            onBack = {},
            onProductClick = {},
            onRetry = {},
        )
    }
}
