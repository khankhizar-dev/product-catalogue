package com.example.catalogue.domain.model

data class Product(
    val id: String,
    val title: String,
    val description: String,
    val category: String,
    val price: Double,
    val imageUrl: String,
    val ratingRate: Double,
    val ratingCount: Int,
)

data class CategoryListing(
    val category: String,
    val topRated: List<Product>,
    val others: List<Product>,
)
