package com.example.catalogue.ui.product

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.catalogue.R
import com.example.catalogue.domain.model.Product
import com.example.catalogue.ui.common.BackButton
import com.example.catalogue.ui.common.ErrorView
import com.example.catalogue.ui.common.LoadingView
import com.example.catalogue.ui.common.UiState
import com.example.catalogue.ui.common.displayName
import com.example.catalogue.ui.common.formatPrice
import com.example.catalogue.ui.theme.CatalogTheme
import com.example.catalogue.ui.theme.ProductImageBackground
import com.example.catalogue.ui.theme.Spacing

@Composable
fun ProductScreen(
    onBack: () -> Unit,
    onEnquire: (String) -> Unit,
    viewModel: ProductViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    ProductScreenContent(
        state = state,
        onBack = onBack,
        onEnquire = onEnquire,
        onRetry = viewModel::load,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductScreenContent(
    state: UiState<Product>,
    onBack: () -> Unit,
    onEnquire: (String) -> Unit,
    onRetry: () -> Unit,
) {
    Scaffold(
        topBar = { TopAppBar(title = { Text(stringResource(R.string.title_product)) }, navigationIcon = { BackButton(onBack) }) },
        bottomBar = {
            (state as? UiState.Success<Product>)?.let { success ->
                Box(Modifier.padding(Spacing.md)) {
                    Button(onClick = { onEnquire(success.data.id) }, modifier = Modifier.fillMaxWidth()) {
                        Text(stringResource(R.string.btn_enquire))
                    }
                }
            }
        },
    ) { padding ->
        Box(Modifier.padding(padding)) {
            when (state) {
                UiState.Loading -> LoadingView()
                is UiState.Error -> ErrorView(state.message, onRetry = onRetry)
                is UiState.Success -> {
                    val product = state.data
                    Column(Modifier.verticalScroll(rememberScrollState())) {
                        AsyncImage(
                            model = product.imageUrl,
                            contentDescription = product.title,
                            contentScale = ContentScale.Fit,
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(1f)
                                .background(ProductImageBackground)
                                .padding(Spacing.md),
                        )
                        Column(Modifier.padding(Spacing.md)) {
                            Text(product.category.displayName(), style = MaterialTheme.typography.labelLarge)
                            Spacer(Modifier.height(Spacing.xs))
                            Text(product.title, style = MaterialTheme.typography.titleLarge)
                            Spacer(Modifier.height(Spacing.sm))
                            Text(
                                formatPrice(product.price),
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                            )
                            Text(
                                pluralStringResource(
                                    R.plurals.product_rating_reviews_format,
                                    product.ratingCount,
                                    product.ratingRate,
                                    product.ratingCount,
                                ),
                                style = MaterialTheme.typography.bodyMedium,
                            )
                            Spacer(Modifier.height(Spacing.md))
                            Text(product.description, style = MaterialTheme.typography.bodyLarge)
                        }
                    }
                }
            }
        }
    }
}

@Preview(name = "Product Detail Screen - Light", showBackground = true)
@Preview(name = "Product Detail Screen - Dark", uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Composable
fun ProductScreenPreview() {
    CatalogTheme {
        ProductScreenContent(
            state = UiState.Success(
                Product(
                    id = "1",
                    title = "Fjallraven - Foldsack No. 1 Backpack",
                    description = "Your everyday pack for items and essentials. Fits 15 laptops securely inside the main compartment.",
                    category = "men's clothing",
                    price = 109.95,
                    imageUrl = "",
                    ratingRate = 3.9,
                    ratingCount = 120,
                )
            ),
            onBack = {},
            onEnquire = {},
            onRetry = {},
        )
    }
}
