package com.donperry.rest.pet

import com.donperry.rest.pet.handler.PetHandler
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

@ExtendWith(MockitoExtension::class)
class PetRouterTest {

    @Mock
    private lateinit var petHandler: PetHandler

    private lateinit var webTestClient: WebTestClient
    private lateinit var petRouter: PetRouter

    @BeforeEach
    fun setUp() {
        petRouter = PetRouter()
        val routerFunction = petRouter.petRoutes(petHandler)
        webTestClient = WebTestClient.bindToRouterFunction(routerFunction).build()
    }

    @Test
    fun `should invoke registerPet handler when POST to api pets with JSON`() {
        // Arrange
        whenever(petHandler.registerPet(any())).thenReturn(
            ServerResponse.status(201).build()
        )

        // Act & Assert
        webTestClient
            .post()
            .uri("/api/pets")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue("""{"name":"Buddy","species":"DOG","breed":"Golden Retriever","age":3}""")
            .exchange()
            .expectStatus().isEqualTo(201)

        verify(petHandler).registerPet(any())
    }

    @Test
    fun `should invoke generatePresignedUrl handler when POST to api pets petId avatar presign with JSON`() {
        // Arrange
        whenever(petHandler.generatePresignedUrl(any())).thenReturn(
            ServerResponse.ok().build()
        )

        // Act & Assert
        webTestClient
            .post()
            .uri("/api/pets/pet-123/avatar/presign")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue("""{"contentType":"image/jpeg"}""")
            .exchange()
            .expectStatus().isOk

        verify(petHandler).generatePresignedUrl(any())
    }

    @Test
    fun `should invoke confirmAvatarUpload handler when POST to api pets petId avatar confirm with JSON`() {
        // Arrange
        whenever(petHandler.confirmAvatarUpload(any())).thenReturn(
            ServerResponse.ok().build()
        )

        // Act & Assert
        webTestClient
            .post()
            .uri("/api/pets/pet-123/avatar/confirm")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue("""{"photoKey":"pets/user-123/pet-123/avatar.jpg"}""")
            .exchange()
            .expectStatus().isOk

        verify(petHandler).confirmAvatarUpload(any())
    }

    @Test
    fun `should return 404 when GET api pets is requested`() {
        // Act & Assert
        webTestClient
            .get()
            .uri("/api/pets")
            .exchange()
            .expectStatus().isNotFound
    }

    @Test
    fun `should return 404 when route without api prefix is requested`() {
        // Act & Assert
        webTestClient
            .post()
            .uri("/pets")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue("""{"name":"Buddy","species":"DOG","age":3}""")
            .exchange()
            .expectStatus().isNotFound
    }
}
