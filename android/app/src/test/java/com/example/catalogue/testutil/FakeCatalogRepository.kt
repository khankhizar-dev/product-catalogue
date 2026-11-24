package com.example.catalogue.testutil

import com.example.catalogue.domain.model.CatalogException
import com.example.catalogue.domain.model.CategoryListing
import com.example.catalogue.domain.model.Enquiry
import com.example.catalogue.domain.model.EnquiryOutcome
import com.example.catalogue.domain.model.Product
import com.example.catalogue.domain.repository.CatalogRepository
import java.io.IOException

fun createProduct(id: String, title: String = "Product $id", category: String = "jewelery") = Product(
    id = id, title = title, description = "Description $id", category = category,
    price = 10.0, imageUrl = "http://img/$id.png", ratingRate = 4.0, ratingCount = 10,
)

class FakeCatalogRepository : CatalogRepository {
    var featuredResult: Result<List<Product>> = Result.success(emptyList())
    var categoriesResult: Result<List<String>> = Result.success(emptyList())
    var categoryResult: Result<CategoryListing> = Result.failure(CatalogException.NotFound())
    var searchResult: Result<List<Product>> = Result.success(emptyList())
    var productResult: Result<Product> = Result.failure(CatalogException.NotFound())
    var enquiryOutcome: EnquiryOutcome = EnquiryOutcome.Sent

    val searchedQueries = mutableListOf<String>()
    val submittedEnquiries = mutableListOf<Enquiry>()

    override suspend fun featured() = featuredResult
    override suspend fun categories() = categoriesResult
    override suspend fun categoryListing(category: String) = categoryResult
    override suspend fun search(query: String): Result<List<Product>> {
        searchedQueries += query
        return searchResult
    }
    override suspend fun product(id: String) = productResult
    override suspend fun submitEnquiry(enquiry: Enquiry): EnquiryOutcome {
        submittedEnquiries += enquiry
        return enquiryOutcome
    }
}

fun networkFailure(): Throwable = CatalogException.Network(IOException("offline"))
