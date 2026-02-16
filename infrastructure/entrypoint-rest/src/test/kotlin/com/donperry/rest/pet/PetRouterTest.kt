package com.donperry.rest.pet

import com.donperry.rest.pet.handler.PetHandler
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.http.MediaType
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.web.reactive.function.server.ServerResponse
import reactor.core.publisher.Mono
import java.util.Base64

@ExtendWith(MockitoExtension::class)
class PetRouterTest {

    @Mock
    private lateinit var petHandler: PetHandler

    private lateinit var webTestClient: WebTestClient
    private lateinit var petRouter: PetRouter

    private val objectMapper = jacksonObjectMapper()

    private fun buildJwt(payload: Map<String, Any>): String {
        val header = Base64.getUrlEncoder().withoutPadding().encodeToString("""{"alg":"none"}""".toByteArray())
        val payloadEncoded = Base64.getUrlEncoder().withoutPadding().encodeToString(
            objectMapper.writeValueAsBytes(payload)
        )
        return "$header.$payloadEncoded.signature"
    }

    @BeforeEach
    fun setUp() {
        petRouter = PetRouter()
        val routerFunction = petRouter.petRoutes(petHandler)
        webTestClient = WebTestClient.bindToRouterFunction(routerFunction).build()
    }

    @Test
    fun `should invoke registerPet handler when POST to api pets with valid JWT`() {
        val jwt = buildJwt(mapOf("user_id" to "user-123"))
        whenever(petHandler.registerPet(any())).thenReturn(
            ServerResponse.status(201).build()
        )

        webTestClient
            .post()
            .uri("/api/pets")
            .contentType(MediaType.APPLICATION_JSON)
            .header("Authorization", "Bearer $jwt")
            .bodyValue("""{"name":"Buddy","species":"DOG","breed":"Golden Retriever","age":3}""")
            .exchange()
            .expectStatus().isEqualTo(201)

        verify(petHandler).registerPet(any())
    }

    @Test
    fun `should return 401 when POST to api pets without Authorization header`() {
        webTestClient
            .post()
            .uri("/api/pets")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue("""{"name":"Buddy","species":"DOG","breed":"Golden Retriever","age":3}""")
            .exchange()
            .expectStatus().isUnauthorized
            .expectBody()
            .jsonPath("$.error").isEqualTo("UNAUTHORIZED")
            .jsonPath("$.message").isEqualTo("Missing or invalid Authorization header")
    }

    @Test
    fun `should return 401 when POST to api pets with invalid JWT format`() {
        webTestClient
            .post()
            .uri("/api/pets")
            .contentType(MediaType.APPLICATION_JSON)
            .header("Authorization", "Bearer invalid.jwt")
            .bodyValue("""{"name":"Buddy","species":"DOG","breed":"Golden Retriever","age":3}""")
            .exchange()
            .expectStatus().isUnauthorized
            .expectBody()
            .jsonPath("$.error").isEqualTo("UNAUTHORIZED")
            .jsonPath("$.message").isEqualTo("Invalid JWT format")
    }

    @Test
    fun `should return 401 when POST to api pets with JWT missing user_id claim`() {
        val jwt = buildJwt(mapOf("sub" to "user-123"))

        webTestClient
            .post()
            .uri("/api/pets")
            .contentType(MediaType.APPLICATION_JSON)
            .header("Authorization", "Bearer $jwt")
            .bodyValue("""{"name":"Buddy","species":"DOG","breed":"Golden Retriever","age":3}""")
            .exchange()
            .expectStatus().isUnauthorized
            .expectBody()
            .jsonPath("$.error").isEqualTo("UNAUTHORIZED")
            .jsonPath("$.message").isEqualTo("Missing user_id claim in JWT")
    }

    @Test
    fun `should invoke generatePresignedUrl handler when POST to api pets petId avatar presign with valid JWT`() {
        val jwt = buildJwt(mapOf("user_id" to "user-123"))
        whenever(petHandler.generatePresignedUrl(any())).thenReturn(
            ServerResponse.ok().build()
        )

        webTestClient
            .post()
            .uri("/api/pets/pet-123/avatar/presign")
            .contentType(MediaType.APPLICATION_JSON)
            .header("Authorization", "Bearer $jwt")
            .bodyValue("""{"contentType":"image/jpeg"}""")
            .exchange()
            .expectStatus().isOk

        verify(petHandler).generatePresignedUrl(any())
    }

    @Test
    fun `should return 401 when POST to presign endpoint without Authorization header`() {
        webTestClient
            .post()
            .uri("/api/pets/pet-123/avatar/presign")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue("""{"contentType":"image/jpeg"}""")
            .exchange()
            .expectStatus().isUnauthorized
            .expectBody()
            .jsonPath("$.error").isEqualTo("UNAUTHORIZED")
    }

    @Test
    fun `should invoke confirmAvatarUpload handler when POST to api pets petId avatar confirm with valid JWT`() {
        val jwt = buildJwt(mapOf("user_id" to "user-123"))
        whenever(petHandler.confirmAvatarUpload(any())).thenReturn(
            ServerResponse.ok().build()
        )

        webTestClient
            .post()
            .uri("/api/pets/pet-123/avatar/confirm")
            .contentType(MediaType.APPLICATION_JSON)
            .header("Authorization", "Bearer $jwt")
            .bodyValue("""{"photoKey":"pets/user-123/pet-123/avatar.jpg"}""")
            .exchange()
            .expectStatus().isOk

        verify(petHandler).confirmAvatarUpload(any())
    }

    @Test
    fun `should return 401 when POST to confirm endpoint without Authorization header`() {
        webTestClient
            .post()
            .uri("/api/pets/pet-123/avatar/confirm")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue("""{"photoKey":"pets/user-123/pet-123/avatar.jpg"}""")
            .exchange()
            .expectStatus().isUnauthorized
            .expectBody()
            .jsonPath("$.error").isEqualTo("UNAUTHORIZED")
    }

    @Test
    fun `should return 404 when GET api pets is requested`() {
        webTestClient
            .get()
            .uri("/api/pets")
            .exchange()
            .expectStatus().isNotFound
    }

    @Test
    fun `should return 404 when route without api prefix is requested`() {
        webTestClient
            .post()
            .uri("/pets")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue("""{"name":"Buddy","species":"DOG","age":3}""")
            .exchange()
            .expectStatus().isNotFound
    }
}
