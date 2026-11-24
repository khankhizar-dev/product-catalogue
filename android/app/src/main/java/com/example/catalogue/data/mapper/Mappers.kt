package com.example.catalogue.data.mapper

import com.example.catalogue.data.remote.CategoryResponse
import com.example.catalogue.data.remote.EnquiryRequest
import com.example.catalogue.data.remote.ProductDto
import com.example.catalogue.domain.model.CategoryListing
import com.example.catalogue.domain.model.Enquiry
import com.example.catalogue.domain.model.Product

fun ProductDto.toDomain() = Product(
    id = id,
    title = title,
    description = description,
    category = category,
    price = price,
    imageUrl = imageUrl,
    ratingRate = rating.rate,
    ratingCount = rating.count,
)

fun CategoryResponse.toDomain() = CategoryListing(
    category = category,
    topRated = topRated.map { it.toDomain() },
    others = others.map { it.toDomain() },
)

fun Enquiry.toRequest() = EnquiryRequest(
    productId = productId,
    name = name,
    email = email,
    phone = phone,
    message = message,
)
