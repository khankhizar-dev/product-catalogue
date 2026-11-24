package com.example.catalogue.ui.enquiry

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.catalogue.R
import com.example.catalogue.ui.common.BackButton
import com.example.catalogue.ui.theme.Spacing

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EnquiryScreen(
    onBack: () -> Unit,
    onDone: () -> Unit,
    viewModel: EnquiryViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    Scaffold(
        topBar = { TopAppBar(title = { Text(stringResource(R.string.title_enquire)) }, navigationIcon = { BackButton(onBack) }) },
    ) { padding ->
        if (state.sent) {
            ThankYou(onDone, Modifier.padding(padding))
        } else {
            EnquiryFormContent(state, viewModel, Modifier.padding(padding))
        }
    }
}

@Composable
private fun ThankYou(onDone: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier.fillMaxSize().padding(Spacing.lg),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(stringResource(R.string.thank_you_title), style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(Spacing.sm))
        Text(
            stringResource(R.string.thank_you_message),
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(Spacing.lg))
        Button(onClick = onDone) { Text(stringResource(R.string.btn_back_to_catalogue)) }
    }
}

@Composable
private fun EnquiryFormContent(state: EnquiryUiState, viewModel: EnquiryViewModel, modifier: Modifier = Modifier) {
    val form = state.form
    Column(
        modifier
            .fillMaxSize()
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(Spacing.md),
        verticalArrangement = Arrangement.spacedBy(Spacing.sm),
    ) {
        state.productTitle?.let {
            Text(stringResource(R.string.enquiry_about_product, it), style = MaterialTheme.typography.titleMedium)
        }
        FormField(
            label = stringResource(R.string.label_name),
            value = form.name,
            error = state.fieldErrors["name"]?.asString(),
        ) { viewModel.onFormChange(form.copy(name = it)) }

        FormField(
            label = stringResource(R.string.label_email),
            value = form.email,
            error = state.fieldErrors["email"]?.asString(),
            keyboardType = KeyboardType.Email,
        ) { viewModel.onFormChange(form.copy(email = it)) }

        FormField(
            label = stringResource(R.string.label_phone_optional),
            value = form.phone,
            error = state.fieldErrors["phone"]?.asString(),
            keyboardType = KeyboardType.Phone,
        ) { viewModel.onFormChange(form.copy(phone = it)) }

        FormField(
            label = stringResource(R.string.label_message),
            value = form.message,
            error = state.fieldErrors["message"]?.asString(),
            singleLine = false,
        ) { viewModel.onFormChange(form.copy(message = it)) }

        state.error?.let {
            Text(it.asString(), color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium)
        }
        Button(
            onClick = viewModel::submit,
            enabled = !state.submitting,
            modifier = Modifier.fillMaxWidth(),
        ) {
            if (state.submitting) {
                CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp)
            } else {
                Text(stringResource(R.string.btn_send_enquiry))
            }
        }
    }
}

@Composable
private fun FormField(
    label: String,
    value: String,
    error: String?,
    keyboardType: KeyboardType = KeyboardType.Text,
    singleLine: Boolean = true,
    onChange: (String) -> Unit,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onChange,
        label = { Text(label) },
        isError = error != null,
        supportingText = if (error != null) {
            { Text(error) }
        } else {
            null
        },
        singleLine = singleLine,
        minLines = if (singleLine) 1 else 4,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        modifier = Modifier.fillMaxWidth(),
    )
}
