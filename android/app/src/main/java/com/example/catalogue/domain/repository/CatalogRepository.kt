package com.example.catalogue.domain.repository

import com.example.catalogue.domain.model.CategoryListing
import com.example.catalogue.domain.model.Enquiry
import com.example.catalogue.domain.model.EnquiryOutcome
import com.example.catalogue.domain.model.Product

/** What the UI knows about the catalogue. Where data comes from is not its concern. */
interface CatalogRepository {
    suspend fun featured(): Result<List<Product>>
    suspend fun categories(): Result<List<String>>
    suspend fun categoryListing(category: String): Result<CategoryListing>
    suspend fun search(query: String): Result<List<Product>>
    suspend fun product(id: String): Result<Product>
    suspend fun submitEnquiry(enquiry: Enquiry): EnquiryOutcome
}
