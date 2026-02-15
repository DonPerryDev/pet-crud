package com.donperry.rest.pet

import com.donperry.rest.pet.handler.PetHandler
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.junit.jupiter.MockitoExtension
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
    fun `POST api pets route should be configured`() {
        `when`(petHandler.registerPet(org.mockito.kotlin.any())).thenReturn(
            ServerResponse.ok().build()
        )

        webTestClient
            .post()
            .uri("/api/pets")
            .contentType(MediaType.MULTIPART_FORM_DATA)
            .exchange()
            .expectStatus().isOk
    }

    @Test
    fun `GET api pets should return 404 for non-configured routes`() {
        webTestClient
            .get()
            .uri("/api/pets")
            .exchange()
            .expectStatus().isNotFound
    }

    @Test
    fun `POST without api pets prefix should return 404`() {
        webTestClient
            .post()
            .uri("/pets")
            .exchange()
            .expectStatus().isNotFound
    }
}
