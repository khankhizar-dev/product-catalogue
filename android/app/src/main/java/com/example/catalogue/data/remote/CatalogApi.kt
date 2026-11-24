package com.example.catalogue.data.remote

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

/** The backend contract. See the backend README for the endpoint list. */
interface CatalogApi {
    @GET("api/products/featured")
    suspend fun featured(): ProductsResponse

    @GET("api/categories")
    suspend fun categories(): CategoriesResponse

    @GET("api/categories/{category}/products")
    suspend fun category(@Path("category") category: String): CategoryResponse

    @GET("api/products")
    suspend fun search(@Query("q") query: String): ProductsResponse

    @GET("api/products/{id}")
    suspend fun product(@Path("id") id: String): ProductResponse

    @POST("api/enquiries")
    suspend fun submitEnquiry(@Body body: EnquiryRequest): Response<Unit>
}
