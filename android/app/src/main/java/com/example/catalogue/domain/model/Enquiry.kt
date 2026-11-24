package com.example.catalogue.domain.model

data class Enquiry(
    val productId: String,
    val name: String,
    val email: String,
    val phone: String?,
    val message: String,
)

enum class EnquiryFailure {
    Network,
    TooManyRequests,
    Generic,
}

sealed interface EnquiryOutcome {
    /** The backend accepted the enquiry and the sales email was sent. */
    data object Sent : EnquiryOutcome

    /** The backend rejected the input. Keys are field names: name, email, phone, message. */
    data class Invalid(val fieldErrors: Map<String, String>) : EnquiryOutcome

    data class Failed(val reason: EnquiryFailure) : EnquiryOutcome
}
