package com.example.catalogue.domain.model

/** Errors the UI cares about. Data-layer exceptions are translated into these. */
sealed class CatalogException(message: String, cause: Throwable? = null) : Exception(message, cause) {
    class Network(cause: Throwable) : CatalogException("Network error", cause)
    class NotFound : CatalogException("Not found")
    class Server(val code: Int) : CatalogException("Server error $code")
    class Unknown(cause: Throwable) : CatalogException("Unexpected error", cause)
}
