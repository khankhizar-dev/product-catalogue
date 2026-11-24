package com.example.catalogue.ui

import androidx.lifecycle.SavedStateHandle
import com.example.catalogue.testutil.FakeCatalogRepository
import com.example.catalogue.testutil.MainDispatcherRule
import com.example.catalogue.testutil.createProduct
import com.example.catalogue.ui.common.UiState
import com.example.catalogue.ui.search.SearchViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class SearchViewModelTest {
    @get:Rule val mainDispatcher = MainDispatcherRule()

    private fun viewModel(repo: FakeCatalogRepository, query: String = "ring") =
        SearchViewModel(repo, SavedStateHandle(mapOf("query" to query)))

    @Test
    fun `searches for the query it was opened with`() {
        val repo = FakeCatalogRepository().apply { searchResult = Result.success(listOf(createProduct("1"))) }

        val vm = viewModel(repo)

        assertEquals(listOf("ring"), repo.searchedQueries)
        assertEquals(listOf("1"), (vm.state.value as UiState.Success).data.map { it.id })
    }

    @Test
    fun `a new search trims the text and replaces the results`() {
        val repo = FakeCatalogRepository()
        val vm = viewModel(repo)

        vm.search("  gold  ")

        assertEquals(listOf("ring", "gold"), repo.searchedQueries)
        assertEquals("gold", vm.query.value)
    }

    @Test
    fun `a blank search is ignored`() {
        val repo = FakeCatalogRepository()
        val vm = viewModel(repo)

        vm.search("   ")

        assertEquals(listOf("ring"), repo.searchedQueries)
        assertTrue(vm.state.value is UiState.Success)
    }

    @Test
    fun `blank initial query starts with empty results instead of loading forever`() {
        val repo = FakeCatalogRepository()

        val vm = viewModel(repo, query = "")

        assertTrue(vm.state.value is UiState.Success)
        assertTrue((vm.state.value as UiState.Success).data.isEmpty())
        assertTrue(repo.searchedQueries.isEmpty())
    }
}
