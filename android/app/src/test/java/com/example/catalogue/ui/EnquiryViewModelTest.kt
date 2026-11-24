package com.example.catalogue.ui

import androidx.lifecycle.SavedStateHandle
import com.example.catalogue.R
import com.example.catalogue.domain.model.EnquiryFailure
import com.example.catalogue.domain.model.EnquiryOutcome
import com.example.catalogue.testutil.FakeCatalogRepository
import com.example.catalogue.testutil.MainDispatcherRule
import com.example.catalogue.testutil.createProduct
import com.example.catalogue.ui.common.UiText
import com.example.catalogue.ui.enquiry.EnquiryForm
import com.example.catalogue.ui.enquiry.EnquiryViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class EnquiryViewModelTest {
    @get:Rule val mainDispatcher = MainDispatcherRule()

    private val valid = EnquiryForm(
        name = " Ada ", email = "ada@example.com", phone = "", message = " Is it in stock? ",
    )

    private fun viewModel(repo: FakeCatalogRepository) =
        EnquiryViewModel(repo, SavedStateHandle(mapOf("productId" to "7")))

    @Test
    fun `invalid input shows field errors and sends nothing`() {
        val repo = FakeCatalogRepository()
        val vm = viewModel(repo)

        vm.onFormChange(EnquiryForm(name = "", email = "nope", phone = "abc", message = ""))
        vm.submit()

        assertEquals(setOf("name", "email", "phone", "message"), vm.state.value.fieldErrors.keys)
        assertTrue(repo.submittedEnquiries.isEmpty())
    }

    @Test
    fun `valid input is trimmed, sent for the right product, and shows the thank-you state`() {
        val repo = FakeCatalogRepository()
        val vm = viewModel(repo)

        vm.onFormChange(valid)
        vm.submit()

        val sent = repo.submittedEnquiries.single()
        assertEquals("7", sent.productId)
        assertEquals("Ada", sent.name)
        assertEquals("Is it in stock?", sent.message)
        assertNull(sent.phone) // blank phone is sent as absent
        assertTrue(vm.state.value.sent)
    }

    @Test
    fun `server field errors are shown next to the fields`() {
        val repo = FakeCatalogRepository().apply {
            enquiryOutcome = EnquiryOutcome.Invalid(mapOf("email" to "Invalid email"))
        }
        val vm = viewModel(repo)

        vm.onFormChange(valid)
        vm.submit()

        assertEquals(UiText.DynamicString("Invalid email"), vm.state.value.fieldErrors["email"])
        assertFalse(vm.state.value.sent)
    }

    @Test
    fun `a failed send keeps the form, shows an error, and is not a thank-you`() {
        val repo = FakeCatalogRepository().apply {
            enquiryOutcome = EnquiryOutcome.Failed(EnquiryFailure.Generic)
        }
        val vm = viewModel(repo)

        vm.onFormChange(valid)
        vm.submit()

        assertEquals(UiText.StringResource(R.string.error_send_enquiry_failed), vm.state.value.error)
        assertEquals(valid, vm.state.value.form)
        assertFalse(vm.state.value.sent)
        assertFalse(vm.state.value.submitting)
    }

    @Test
    fun `editing a field clears only that field's error`() {
        val vm = viewModel(FakeCatalogRepository())
        vm.onFormChange(EnquiryForm(name = "", email = "nope", message = "hi"))
        vm.submit()
        assertEquals(setOf("name", "email"), vm.state.value.fieldErrors.keys)

        vm.onFormChange(vm.state.value.form.copy(name = "Ada"))

        assertEquals(setOf("email"), vm.state.value.fieldErrors.keys)
    }

    @Test
    fun `shows the product title when it can be loaded`() {
        val repo = FakeCatalogRepository().apply { productResult = Result.success(createProduct("7", title = "Gold Ring")) }

        assertEquals("Gold Ring", viewModel(repo).state.value.productTitle)
    }
}
