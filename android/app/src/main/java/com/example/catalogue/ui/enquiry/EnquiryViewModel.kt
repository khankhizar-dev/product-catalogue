package com.example.catalogue.ui.enquiry

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.catalogue.R
import com.example.catalogue.domain.model.Enquiry
import com.example.catalogue.domain.model.EnquiryFailure
import com.example.catalogue.domain.model.EnquiryOutcome
import com.example.catalogue.domain.repository.CatalogRepository
import com.example.catalogue.ui.common.UiText
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class EnquiryForm(
    val name: String = "",
    val email: String = "",
    val phone: String = "",
    val message: String = "",
)

data class EnquiryUiState(
    val productTitle: String? = null,
    val form: EnquiryForm = EnquiryForm(),
    val fieldErrors: Map<String, UiText> = emptyMap(),
    val submitting: Boolean = false,
    val sent: Boolean = false,
    val error: UiText? = null,
)

private val FIELD_KEYS = setOf("name", "email", "phone", "message")
private val EMAIL_REGEX = Regex("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")
private val PHONE_REGEX = Regex("^[0-9+()\\-\\s]{5,20}$")

/**
 * Quick client-side checks for instant feedback. The backend remains the authority:
 * anything it rejects comes back as field errors and is shown the same way.
 */
fun validateEnquiry(form: EnquiryForm): Map<String, UiText> = buildMap {
    if (form.name.isBlank()) put("name", UiText.StringResource(R.string.validation_name_required))
    if (!EMAIL_REGEX.matches(form.email.trim())) put("email", UiText.StringResource(R.string.validation_email_invalid))
    if (form.phone.isNotBlank() && !PHONE_REGEX.matches(form.phone.trim())) put("phone", UiText.StringResource(R.string.validation_phone_invalid))
    if (form.message.isBlank()) put("message", UiText.StringResource(R.string.validation_message_required))
}

@HiltViewModel
class EnquiryViewModel @Inject constructor(
    private val repository: CatalogRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val productId: String = checkNotNull(savedStateHandle.get<String>("productId"))

    private val _state = MutableStateFlow(EnquiryUiState())
    val state: StateFlow<EnquiryUiState> = _state.asStateFlow()

    init {
        // Only used to show which product the enquiry is about. Failure is not fatal.
        viewModelScope.launch {
            repository.product(productId).onSuccess { product ->
                _state.update { it.copy(productTitle = product.title) }
            }
        }
    }

    fun onFormChange(new: EnquiryForm) = _state.update { s ->
        val old = s.form
        // Clear the error of any field the user just edited.
        val remaining = s.fieldErrors.filterKeys { key ->
            when (key) {
                "name" -> old.name == new.name
                "email" -> old.email == new.email
                "phone" -> old.phone == new.phone
                "message" -> old.message == new.message
                else -> true
            }
        }
        s.copy(form = new, fieldErrors = remaining, error = null)
    }

    fun submit() {
        val current = _state.value
        if (current.submitting || current.sent) return

        val errors = validateEnquiry(current.form)
        if (errors.isNotEmpty()) {
            _state.update { it.copy(fieldErrors = errors) }
            return
        }

        _state.update { it.copy(submitting = true, error = null, fieldErrors = emptyMap()) }
        val form = current.form
        viewModelScope.launch {
            val outcome = repository.submitEnquiry(
                Enquiry(
                    productId = productId,
                    name = form.name.trim(),
                    email = form.email.trim(),
                    phone = form.phone.trim().ifEmpty { null },
                    message = form.message.trim(),
                ),
            )
            _state.update { s ->
                when (outcome) {
                    EnquiryOutcome.Sent -> s.copy(submitting = false, sent = true)
                    is EnquiryOutcome.Invalid -> {
                        val known = outcome.fieldErrors.filterKeys { it in FIELD_KEYS }
                            .mapValues { UiText.DynamicString(it.value) }
                        s.copy(
                            submitting = false,
                            fieldErrors = known,
                            error = if (known.isEmpty()) UiText.StringResource(R.string.error_check_details) else null,
                        )
                    }
                    is EnquiryOutcome.Failed -> s.copy(
                        submitting = false,
                        error = UiText.StringResource(
                            when (outcome.reason) {
                                EnquiryFailure.Network -> R.string.error_network
                                EnquiryFailure.TooManyRequests -> R.string.error_too_many_enquiries
                                EnquiryFailure.Generic -> R.string.error_send_enquiry_failed
                            },
                        ),
                    )
                }
            }
        }
    }
}
