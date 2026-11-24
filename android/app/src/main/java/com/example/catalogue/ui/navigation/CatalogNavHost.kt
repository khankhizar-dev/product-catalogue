package com.example.catalogue.ui.navigation

import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.catalogue.ui.category.CategoryScreen
import com.example.catalogue.ui.enquiry.EnquiryScreen
import com.example.catalogue.ui.home.HomeScreen
import com.example.catalogue.ui.product.ProductScreen
import com.example.catalogue.ui.search.SearchScreen

object Routes {
    const val HOME = "home"
    const val CATEGORY = "category/{category}"
    const val SEARCH = "search/{query}"
    const val PRODUCT = "product/{id}"
    const val ENQUIRY = "enquiry/{productId}"

    // Values are encoded because categories like "men's clothing" and free-text queries contain
    // spaces, apostrophes or slashes.
    fun category(name: String) = "category/${Uri.encode(name)}"
    fun search(query: String) = "search/${Uri.encode(query)}"
    fun product(id: String) = "product/${Uri.encode(id)}"
    fun enquiry(productId: String) = "enquiry/${Uri.encode(productId)}"
}

@Composable
fun CatalogNavHost() {
    val nav = rememberNavController()
    NavHost(navController = nav, startDestination = Routes.HOME) {
        composable(Routes.HOME) {
            HomeScreen(
                onProductClick = { nav.navigate(Routes.product(it)) },
                onCategoryClick = { nav.navigate(Routes.category(it)) },
                onSearch = { nav.navigate(Routes.search(it)) },
            )
        }
        composable(Routes.CATEGORY) {
            CategoryScreen(
                onBack = { nav.popBackStack() },
                onProductClick = { nav.navigate(Routes.product(it)) },
            )
        }
        composable(Routes.SEARCH) {
            SearchScreen(
                onBack = { nav.popBackStack() },
                onProductClick = { nav.navigate(Routes.product(it)) },
            )
        }
        composable(Routes.PRODUCT) {
            ProductScreen(
                onBack = { nav.popBackStack() },
                onEnquire = { nav.navigate(Routes.enquiry(it)) },
            )
        }
        composable(Routes.ENQUIRY) {
            EnquiryScreen(
                onBack = { nav.popBackStack() },
                onDone = { nav.popBackStack(Routes.HOME, inclusive = false) },
            )
        }
    }
}
