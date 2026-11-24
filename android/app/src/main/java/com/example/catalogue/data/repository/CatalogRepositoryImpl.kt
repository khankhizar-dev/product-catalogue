package com.example.catalogue.data.repository

import com.example.catalogue.data.mapper.toDomain
import com.example.catalogue.data.mapper.toRequest
import com.example.catalogue.data.remote.ApiErrorDto
import com.example.catalogue.data.remote.CatalogApi
import com.example.catalogue.domain.model.CatalogException
import com.example.catalogue.domain.model.CategoryListing
import com.example.catalogue.domain.model.Enquiry
import com.example.catalogue.domain.model.EnquiryFailure
import com.example.catalogue.domain.model.EnquiryOutcome
import com.example.catalogue.domain.model.Product
import com.example.catalogue.domain.repository.CatalogRepository
import kotlinx.coroutines.CancellationException
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CatalogRepositoryImpl @Inject constructor(
    private val api: CatalogApi,
    private val json: Json,
) : CatalogRepository {

    override suspend fun featured(): Result<List<Product>> =
        call { api.featured().products.map { it.toDomain() } }

    override suspend fun categories(): Result<List<String>> =
        call { api.categories().categories }

    override suspend fun categoryListing(category: String): Result<CategoryListing> =
        call { api.category(category).toDomain() }

    override suspend fun search(query: String): Result<List<Product>> =
        call { api.search(query).products.map { it.toDomain() } }

    override suspend fun product(id: String): Result<Product> =
        call { api.product(id).product.toDomain() }

    override suspend fun submitEnquiry(enquiry: Enquiry): EnquiryOutcome =
        try {
            val response = api.submitEnquiry(enquiry.toRequest())
            when {
                response.isSuccessful -> EnquiryOutcome.Sent
                response.code() == 400 -> EnquiryOutcome.Invalid(parseFieldErrors(response.errorBody()?.string()))
                response.code() == 429 -> EnquiryOutcome.Failed(EnquiryFailure.TooManyRequests)
                else -> EnquiryOutcome.Failed(EnquiryFailure.Generic)
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: IOException) {
            EnquiryOutcome.Failed(EnquiryFailure.Network)
        } catch (e: SerializationException) {
            EnquiryOutcome.Failed(EnquiryFailure.Network)
        } catch (e: Exception) {
            EnquiryOutcome.Failed(EnquiryFailure.Generic)
        }

    private fun parseFieldErrors(body: String?): Map<String, String> =
        runCatching {
            json.decodeFromString<ApiErrorDto>(body.orEmpty())
                .details.orEmpty()
                .associate { it.field to it.message }
        }.getOrDefault(emptyMap())

    /** Runs a request and translates failures into [CatalogException]. Never swallows cancellation. */
    private suspend fun <T> call(block: suspend () -> T): Result<T> =
        try {
            Result.success(block())
        } catch (e: CancellationException) {
            throw e
        } catch (e: HttpException) {
            Result.failure(
                if (e.code() == 404) CatalogException.NotFound() else CatalogException.Server(e.code()),
            )
        } catch (e: IOException) {
            Result.failure(CatalogException.Network(e))
        } catch (e: SerializationException) {
            Result.failure(CatalogException.Network(e))
        } catch (e: Exception) {
            Result.failure(CatalogException.Unknown(e))
        }
}
