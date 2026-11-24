package com.example.catalogue.data.remote

import kotlinx.serialization.Serializable

@Serializable
data class RatingDto(val rate: Double = 0.0, val count: Int = 0)

@Serializable
data class ProductDto(
    val id: String,
    val title: String,
    val description: String,
    val category: String,
    val price: Double,
    val imageUrl: String,
    val rating: RatingDto = RatingDto(),
)

@Serializable
data class ProductsResponse(val products: List<ProductDto>)

@Serializable
data class ProductResponse(val product: ProductDto)

@Serializable
data class CategoriesResponse(val categories: List<String>)

@Serializable
data class CategoryResponse(
    val category: String,
    val topRated: List<ProductDto>,
    val others: List<ProductDto>,
)

@Serializable
data class EnquiryRequest(
    val productId: String,
    val name: String,
    val email: String,
    val phone: String? = null,
    val message: String,
)

@Serializable
data class FieldErrorDto(val field: String, val message: String)

@Serializable
data class ApiErrorDto(val error: String? = null, val details: List<FieldErrorDto>? = null)
