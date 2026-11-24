package com.example.catalogue.ui

import com.example.catalogue.testutil.FakeCatalogRepository
import com.example.catalogue.testutil.MainDispatcherRule
import com.example.catalogue.testutil.createProduct
import com.example.catalogue.testutil.networkFailure
import com.example.catalogue.ui.common.UiState
import com.example.catalogue.ui.home.HomeViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class HomeViewModelTest {
    @get:Rule val mainDispatcher = MainDispatcherRule()

    @Test
    fun `shows featured products and categories`() {
        val repo = FakeCatalogRepository().apply {
            featuredResult = Result.success(listOf(createProduct("1"), createProduct("2")))
            categoriesResult = Result.success(listOf("jewelery", "electronics"))
        }

        val state = HomeViewModel(repo).state.value

        assertTrue(state is UiState.Success)
        val content = (state as UiState.Success).data
        assertEquals(listOf("1", "2"), content.featured.map { it.id })
        assertEquals(listOf("jewelery", "electronics"), content.categories)
    }

    @Test
    fun `shows an error when loading fails and recovers on retry`() {
        val repo = FakeCatalogRepository().apply { featuredResult = Result.failure(networkFailure()) }
        val viewModel = HomeViewModel(repo)
        assertTrue(viewModel.state.value is UiState.Error)

        repo.featuredResult = Result.success(listOf(createProduct("1")))
        viewModel.load()

        assertTrue(viewModel.state.value is UiState.Success)
    }
}
