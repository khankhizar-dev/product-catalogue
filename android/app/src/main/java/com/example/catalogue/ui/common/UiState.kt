package com.example.catalogue.ui.common

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.example.catalogue.R
import com.example.catalogue.domain.model.CatalogException

sealed interface UiText {
    data class DynamicString(val value: String) : UiText
    class StringResource(
        @get:StringRes val resId: Int,
        val args: List<Any> = emptyList(),
    ) : UiText {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is StringResource) return false
            return (resId == other.resId) && (args == other.args)
        }

        override fun hashCode(): Int {
            var result = resId
            result = (31 * result) + args.hashCode()
            return result
        }
    }

    @Composable
    fun asString(): String = when (this) {
        is DynamicString -> value
        is StringResource -> stringResource(resId, *args.toTypedArray())
    }
}

sealed interface UiState<out T> {
    data object Loading : UiState<Nothing>
    data class Success<T>(val data: T) : UiState<T>
    data class Error(val message: UiText) : UiState<Nothing>
}

fun Throwable.toUserMessage(): UiText = when (this) {
    is CatalogException.Network -> UiText.StringResource(R.string.error_network)
    is CatalogException.NotFound -> UiText.StringResource(R.string.error_not_found)
    else -> UiText.StringResource(R.string.error_generic)
}

fun <T> Result<T>.toUiState(): UiState<T> =
    fold(onSuccess = { UiState.Success(it) }) { UiState.Error(it.toUserMessage()) }
