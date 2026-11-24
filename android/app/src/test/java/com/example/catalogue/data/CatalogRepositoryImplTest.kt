package com.example.catalogue.data

import com.example.catalogue.data.remote.CatalogApi
import com.example.catalogue.data.repository.CatalogRepositoryImpl
import com.example.catalogue.domain.model.CatalogException
import com.example.catalogue.domain.model.EnquiryOutcome
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.SocketPolicy
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory

/** Exercises the real Retrofit + JSON stack against canned backend responses (the API contract). */
class CatalogRepositoryImplTest {
    private lateinit var server: MockWebServer
    private lateinit var repository: CatalogRepositoryImpl

    private val productJson = """
        {"id":"1","title":"Gold Ring","description":"Nice","category":"jewelery","price":9.5,
         "imageUrl":"http://img/1.png","rating":{"rate":4.1,"count":12}}
    """.trimIndent()

    private val enquiry = com.example.catalogue.domain.model.Enquiry("1", "Ada", "ada@example.com", null, "Hello")

    @Before
    fun setUp() {
        server = MockWebServer().apply { start() }
        val json = Json { ignoreUnknownKeys = true }
        val api = Retrofit.Builder()
            .baseUrl(server.url("/"))
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
            .create(CatalogApi::class.java)
        repository = CatalogRepositoryImpl(api, json)
    }

    @After
    fun tearDown() = server.shutdown()

    private fun reply(code: Int, body: String = "") = server.enqueue(MockResponse().setResponseCode(code).setBody(body))

    @Test
    fun `featured maps the backend JSON to domain products`() = runTest {
        reply(200, """{"products":[$productJson]}""")

        val products = repository.featured().getOrThrow()

        assertEquals("Gold Ring", products.single().title)
        assertEquals(4.1, products.single().ratingRate, 0.0)
        assertEquals("/api/products/featured", server.takeRequest().path)
    }

    @Test
    fun `category listing keeps the top rated and other groups apart`() = runTest {
        reply(200, """{"category":"jewelery","topRated":[$productJson],"others":[]}""")

        val listing = repository.categoryListing("men's clothing").getOrThrow()

        assertEquals(1, listing.topRated.size)
        assertTrue(listing.others.isEmpty())
        val path = server.takeRequest().path.orEmpty()
        assertTrue(path.startsWith("/api/categories/men") && path.endsWith("/products"))
    }

    @Test
    fun `search sends the query`() = runTest {
        reply(200, """{"query":"gold ring","products":[]}""")

        repository.search("gold ring").getOrThrow()

        assertEquals("/api/products?q=gold%20ring", server.takeRequest().path)
    }

    @Test
    fun `404 becomes NotFound and network loss becomes Network`() = runTest {
        reply(404, """{"error":"not_found"}""")
        assertTrue(repository.product("999").exceptionOrNull() is CatalogException.NotFound)

        server.enqueue(MockResponse().setSocketPolicy(SocketPolicy.DISCONNECT_AT_START))
        assertTrue(repository.product("1").exceptionOrNull() is CatalogException.Network)
    }

    @Test
    fun `enquiry 201 is Sent`() = runTest {
        reply(201, """{"status":"received"}""")

        assertEquals(EnquiryOutcome.Sent, repository.submitEnquiry(enquiry))
    }

    @Test
    fun `enquiry 400 returns the field errors from the backend`() = runTest {
        reply(400, """{"error":"validation_error","details":[{"field":"email","message":"Invalid email"}]}""")

        val outcome = repository.submitEnquiry(enquiry)

        assertEquals(EnquiryOutcome.Invalid(mapOf("email" to "Invalid email")), outcome)
    }

    @Test
    fun `enquiry 429, 502 and connection loss are failures, never a thank-you`() = runTest {
        reply(429)
        reply(502, """{"error":"email_unavailable"}""")
        reply(500)

        repeat(3) { assertTrue(repository.submitEnquiry(enquiry) is EnquiryOutcome.Failed) }
    }
}
