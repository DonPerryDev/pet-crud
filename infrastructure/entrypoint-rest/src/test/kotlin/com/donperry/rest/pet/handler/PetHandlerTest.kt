package com.donperry.rest.pet.handler

import com.donperry.model.pet.Pet
import com.donperry.rest.pet.dto.RegisterPetRequest
import com.donperry.usecase.pet.RegisterPetUseCase
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.verify
import org.springframework.mock.web.reactive.function.server.MockServerRequest
import org.springframework.web.reactive.function.server.ServerRequest
import reactor.core.publisher.Mono
import reactor.test.StepVerifier
import java.time.LocalDate

@ExtendWith(MockitoExtension::class)
class PetHandlerTest {

    @Mock
    private lateinit var registerPetUseCase: RegisterPetUseCase

    private lateinit var petHandler: PetHandler

    @BeforeEach
    fun setUp() {
        petHandler = PetHandler(registerPetUseCase)
    }

    @Test
    fun `registerPet should process valid request and return ok response`() {
        // Given
        val request = RegisterPetRequest(
            name = "Buddy",
            species = "Dog",
            breed = "Golden Retriever",
            age = 3,
            owner = "John Doe"
        )
        
        val expectedPet = Pet(
            id = "pet-123",
            name = "Buddy",
            species = "Dog",
            breed = "Golden Retriever",
            age = 3,
            owner = "John Doe",
            registrationDate = LocalDate.now()
        )

        val serverRequest = MockServerRequest.builder()
            .body(Mono.just(request))

        `when`(registerPetUseCase.execute("Buddy", "Dog", "Golden Retriever", 3, "John Doe"))
            .thenReturn(Mono.just(expectedPet))

        // When
        val result = petHandler.registerPet(serverRequest)

        // Then
        StepVerifier.create(result)
            .expectNextMatches { serverResponse ->
                serverResponse.statusCode().value() == 200
            }
            .verifyComplete()

        verify(registerPetUseCase).execute("Buddy", "Dog", "Golden Retriever", 3, "John Doe")
    }

    @Test
    fun `registerPet should handle null breed correctly`() {
        // Given
        val request = RegisterPetRequest(
            name = "Mittens",
            species = "Cat",
            breed = null,
            age = 2,
            owner = "Jane Smith"
        )
        
        val expectedPet = Pet(
            id = "pet-456",
            name = "Mittens",
            species = "Cat",
            breed = null,
            age = 2,
            owner = "Jane Smith",
            registrationDate = LocalDate.now()
        )

        val serverRequest = MockServerRequest.builder()
            .body(Mono.just(request))

        `when`(registerPetUseCase.execute("Mittens", "Cat", null, 2, "Jane Smith"))
            .thenReturn(Mono.just(expectedPet))

        // When
        val result = petHandler.registerPet(serverRequest)

        // Then
        StepVerifier.create(result)
            .expectNextMatches { serverResponse ->
                serverResponse.statusCode().value() == 200
            }
            .verifyComplete()

        verify(registerPetUseCase).execute("Mittens", "Cat", null, 2, "Jane Smith")
    }

    @Test
    fun `registerPet should return bad request when use case fails`() {
        // Given
        val request = RegisterPetRequest(
            name = "Buddy",
            species = "Dog",
            breed = "Golden Retriever",
            age = 3,
            owner = "John Doe"
        )

        val serverRequest = MockServerRequest.builder()
            .body(Mono.just(request))

        `when`(registerPetUseCase.execute(any(), any(), any(), any(), any()))
            .thenReturn(Mono.error(RuntimeException("Database connection failed")))

        // When
        val result = petHandler.registerPet(serverRequest)

        // Then
        StepVerifier.create(result)
            .expectNextMatches { serverResponse ->
                serverResponse.statusCode().value() == 400
            }
            .verifyComplete()
    }

    @Test
    fun `registerPet should handle malformed request body`() {
        // Given
        val serverRequest = MockServerRequest.builder()
            .body(Mono.error<RegisterPetRequest>(RuntimeException("Malformed JSON")))

        // When
        val result = petHandler.registerPet(serverRequest)

        // Then
        StepVerifier.create(result)
            .expectNextMatches { serverResponse ->
                serverResponse.statusCode().value() == 400
            }
            .verifyComplete()
    }
}