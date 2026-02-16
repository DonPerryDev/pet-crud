package com.donperry.rest.pet.handler

import com.donperry.model.exception.PetLimitExceededException
import com.donperry.model.exception.PetNotFoundException
import com.donperry.model.exception.PhotoNotFoundException
import com.donperry.model.exception.UnauthorizedException
import com.donperry.model.exception.ValidationException
import com.donperry.model.pet.Pet
import com.donperry.model.pet.PresignedUploadUrl
import com.donperry.model.pet.Species
import com.donperry.rest.common.dto.ErrorResponse
import com.donperry.rest.pet.dto.ConfirmAvatarUploadRequest
import com.donperry.rest.pet.dto.GeneratePresignedUrlRequest
import com.donperry.rest.pet.dto.PetResponse
import com.donperry.rest.pet.dto.PresignedUrlResponse
import com.donperry.rest.pet.dto.RegisterPetRequest
import com.donperry.usecase.pet.ConfirmAvatarUploadUseCase
import com.donperry.usecase.pet.GenerateAvatarPresignedUrlUseCase
import com.donperry.usecase.pet.RegisterPetUseCase
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever
import org.springframework.http.HttpStatus
import org.springframework.web.reactive.function.server.EntityResponse
import org.springframework.web.reactive.function.server.ServerRequest
import reactor.core.publisher.Mono
import reactor.test.StepVerifier
import java.math.BigDecimal
import java.time.Instant
import java.time.LocalDate
import java.util.Optional

@ExtendWith(MockitoExtension::class)
class PetHandlerTest {

    @Mock
    private lateinit var registerPetUseCase: RegisterPetUseCase

    @Mock
    private lateinit var generateAvatarPresignedUrlUseCase: GenerateAvatarPresignedUrlUseCase

    @Mock
    private lateinit var confirmAvatarUploadUseCase: ConfirmAvatarUploadUseCase

    @Mock
    private lateinit var serverRequest: ServerRequest

    @InjectMocks
    private lateinit var petHandler: PetHandler

    // ==================== registerPet Tests ====================

    @Test
    fun `should return 201 with PetResponse when pet registration is successful`() {
        // Arrange
        val userId = "user-123"
        val request = RegisterPetRequest(
            name = "Buddy",
            species = "DOG",
            breed = "Golden Retriever",
            age = 3,
            birthdate = LocalDate.of(2021, 1, 15),
            weight = BigDecimal("25.5"),
            nickname = "Bud"
        )

        val expectedPet = Pet(
            id = "pet-123",
            name = "Buddy",
            species = Species.DOG,
            breed = "Golden Retriever",
            age = 3,
            birthdate = LocalDate.of(2021, 1, 15),
            weight = BigDecimal("25.5"),
            nickname = "Bud",
            owner = userId,
            registrationDate = LocalDate.now(),
            photoUrl = null
        )

        whenever(serverRequest.bodyToMono(RegisterPetRequest::class.java))
            .thenReturn(Mono.just(request))
        whenever(serverRequest.attribute("userId")).thenReturn(Optional.of(userId))
        whenever(registerPetUseCase.execute(any())).thenReturn(Mono.just(expectedPet))

        // Act & Assert
        StepVerifier.create(petHandler.registerPet(serverRequest))
            .expectNextMatches { response ->
                response.statusCode() == HttpStatus.CREATED &&
                    (response as EntityResponse<*>).let { entityResponse ->
                        val body = entityResponse.entity() as PetResponse
                        body.id == "pet-123" &&
                            body.name == "Buddy" &&
                            body.species == "DOG" &&
                            body.breed == "Golden Retriever" &&
                            body.age == 3 &&
                            body.owner == userId &&
                            body.photoUrl == null
                    }
            }
            .verifyComplete()
    }

    @Test
    fun `should return 400 when species is invalid`() {
        // Arrange
        val userId = "user-123"
        val request = RegisterPetRequest(
            name = "Buddy",
            species = "LIZARD",
            breed = null,
            age = 3,
            birthdate = null,
            weight = null,
            nickname = null
        )

        whenever(serverRequest.bodyToMono(RegisterPetRequest::class.java))
            .thenReturn(Mono.just(request))
        whenever(serverRequest.attribute("userId")).thenReturn(Optional.of(userId))

        // Act & Assert
        StepVerifier.create(petHandler.registerPet(serverRequest))
            .expectNextMatches { response ->
                response.statusCode() == HttpStatus.BAD_REQUEST &&
                    (response as EntityResponse<*>).let { entityResponse ->
                        val body = entityResponse.entity() as ErrorResponse
                        body.error == "VALIDATION_ERROR" &&
                            body.message.contains("Invalid species: LIZARD")
                    }
            }
            .verifyComplete()
    }

    @Test
    fun `should return 401 when no authentication context is found for registerPet`() {
        // Arrange
        whenever(serverRequest.attribute("userId")).thenReturn(Optional.empty())

        // Act & Assert
        StepVerifier.create(petHandler.registerPet(serverRequest))
            .expectNextMatches { response ->
                response.statusCode() == HttpStatus.UNAUTHORIZED &&
                    (response as EntityResponse<*>).let { entityResponse ->
                        val body = entityResponse.entity() as ErrorResponse
                        body.error == "UNAUTHORIZED" &&
                            body.message == "No authentication found"
                    }
            }
            .verifyComplete()
    }

    @Test
    fun `should return 409 when use case throws PetLimitExceededException`() {
        // Arrange
        val userId = "user-123"
        val request = RegisterPetRequest(
            name = "Buddy",
            species = "DOG",
            breed = null,
            age = 3,
            birthdate = null,
            weight = null,
            nickname = null
        )

        whenever(serverRequest.bodyToMono(RegisterPetRequest::class.java))
            .thenReturn(Mono.just(request))
        whenever(serverRequest.attribute("userId")).thenReturn(Optional.of(userId))
        whenever(registerPetUseCase.execute(any()))
            .thenReturn(Mono.error(PetLimitExceededException(userId)))

        // Act & Assert
        StepVerifier.create(petHandler.registerPet(serverRequest))
            .expectNextMatches { response ->
                response.statusCode() == HttpStatus.CONFLICT &&
                    (response as EntityResponse<*>).let { entityResponse ->
                        val body = entityResponse.entity() as ErrorResponse
                        body.error == "PET_LIMIT_EXCEEDED"
                    }
            }
            .verifyComplete()
    }

    @Test
    fun `should return 400 when use case throws ValidationException`() {
        // Arrange
        val userId = "user-123"
        val request = RegisterPetRequest(
            name = "",
            species = "DOG",
            breed = null,
            age = 3,
            birthdate = null,
            weight = null,
            nickname = null
        )

        whenever(serverRequest.bodyToMono(RegisterPetRequest::class.java))
            .thenReturn(Mono.just(request))
        whenever(serverRequest.attribute("userId")).thenReturn(Optional.of(userId))

        // Act & Assert
        StepVerifier.create(petHandler.registerPet(serverRequest))
            .expectNextMatches { response ->
                response.statusCode() == HttpStatus.BAD_REQUEST &&
                    (response as EntityResponse<*>).let { entityResponse ->
                        val body = entityResponse.entity() as ErrorResponse
                        body.error == "VALIDATION_ERROR" &&
                            body.message == "Pet name cannot be blank"
                    }
            }
            .verifyComplete()
    }

    @Test
    fun `should return 500 when use case throws unexpected RuntimeException`() {
        // Arrange
        val userId = "user-123"
        val request = RegisterPetRequest(
            name = "Buddy",
            species = "DOG",
            breed = null,
            age = 3,
            birthdate = null,
            weight = null,
            nickname = null
        )

        whenever(serverRequest.bodyToMono(RegisterPetRequest::class.java))
            .thenReturn(Mono.just(request))
        whenever(serverRequest.attribute("userId")).thenReturn(Optional.of(userId))
        whenever(registerPetUseCase.execute(any()))
            .thenReturn(Mono.error(RuntimeException("Unexpected database error")))

        // Act & Assert
        StepVerifier.create(petHandler.registerPet(serverRequest))
            .expectNextMatches { response ->
                response.statusCode() == HttpStatus.INTERNAL_SERVER_ERROR &&
                    (response as EntityResponse<*>).let { entityResponse ->
                        val body = entityResponse.entity() as ErrorResponse
                        body.error == "INTERNAL_ERROR" &&
                            body.message == "Unexpected database error"
                    }
            }
            .verifyComplete()
    }

    // ==================== generatePresignedUrl Tests ====================

    @Test
    fun `should return 200 with PresignedUrlResponse when URL generation is successful`() {
        // Arrange
        val userId = "user-123"
        val petId = "pet-123"
        val request = GeneratePresignedUrlRequest(contentType = "image/jpeg")

        val presignedUrl = PresignedUploadUrl(
            uploadUrl = "https://s3.amazonaws.com/bucket/upload-url",
            key = "pets/user-123/pet-123/avatar.jpg",
            expiresAt = Instant.parse("2026-02-15T10:00:00Z")
        )

        whenever(serverRequest.pathVariable("petId")).thenReturn(petId)
        whenever(serverRequest.bodyToMono(GeneratePresignedUrlRequest::class.java))
            .thenReturn(Mono.just(request))
        whenever(serverRequest.attribute("userId")).thenReturn(Optional.of(userId))
        whenever(generateAvatarPresignedUrlUseCase.execute(any()))
            .thenReturn(Mono.just(presignedUrl))

        // Act & Assert
        StepVerifier.create(petHandler.generatePresignedUrl(serverRequest))
            .expectNextMatches { response ->
                response.statusCode() == HttpStatus.OK &&
                    (response as EntityResponse<*>).let { entityResponse ->
                        val body = entityResponse.entity() as PresignedUrlResponse
                        body.uploadUrl == "https://s3.amazonaws.com/bucket/upload-url" &&
                            body.key == "pets/user-123/pet-123/avatar.jpg" &&
                            body.expiresAt == "2026-02-15T10:00:00Z"
                    }
            }
            .verifyComplete()
    }

    @Test
    fun `should return 401 when no authentication context is found for generatePresignedUrl`() {
        // Arrange
        val petId = "pet-123"
        whenever(serverRequest.pathVariable("petId")).thenReturn(petId)
        whenever(serverRequest.attribute("userId")).thenReturn(Optional.empty())

        // Act & Assert
        StepVerifier.create(petHandler.generatePresignedUrl(serverRequest))
            .expectNextMatches { response ->
                response.statusCode() == HttpStatus.UNAUTHORIZED &&
                    (response as EntityResponse<*>).let { entityResponse ->
                        val body = entityResponse.entity() as ErrorResponse
                        body.error == "UNAUTHORIZED" &&
                            body.message == "No authentication found"
                    }
            }
            .verifyComplete()
    }

    @Test
    fun `should return 404 when use case throws PetNotFoundException in generatePresignedUrl`() {
        // Arrange
        val userId = "user-123"
        val petId = "pet-999"
        val request = GeneratePresignedUrlRequest(contentType = "image/jpeg")

        whenever(serverRequest.pathVariable("petId")).thenReturn(petId)
        whenever(serverRequest.bodyToMono(GeneratePresignedUrlRequest::class.java))
            .thenReturn(Mono.just(request))
        whenever(serverRequest.attribute("userId")).thenReturn(Optional.of(userId))
        whenever(generateAvatarPresignedUrlUseCase.execute(any()))
            .thenReturn(Mono.error(PetNotFoundException(petId)))

        // Act & Assert
        StepVerifier.create(petHandler.generatePresignedUrl(serverRequest))
            .expectNextMatches { response ->
                response.statusCode() == HttpStatus.NOT_FOUND &&
                    (response as EntityResponse<*>).let { entityResponse ->
                        val body = entityResponse.entity() as ErrorResponse
                        body.error == "PET_NOT_FOUND"
                    }
            }
            .verifyComplete()
    }

    @Test
    fun `should return 401 when use case throws UnauthorizedException in generatePresignedUrl`() {
        // Arrange
        val userId = "user-123"
        val petId = "pet-456"
        val request = GeneratePresignedUrlRequest(contentType = "image/jpeg")

        whenever(serverRequest.pathVariable("petId")).thenReturn(petId)
        whenever(serverRequest.bodyToMono(GeneratePresignedUrlRequest::class.java))
            .thenReturn(Mono.just(request))
        whenever(serverRequest.attribute("userId")).thenReturn(Optional.of(userId))
        whenever(generateAvatarPresignedUrlUseCase.execute(any()))
            .thenReturn(Mono.error(UnauthorizedException("User does not own this pet")))

        // Act & Assert
        StepVerifier.create(petHandler.generatePresignedUrl(serverRequest))
            .expectNextMatches { response ->
                response.statusCode() == HttpStatus.UNAUTHORIZED &&
                    (response as EntityResponse<*>).let { entityResponse ->
                        val body = entityResponse.entity() as ErrorResponse
                        body.error == "UNAUTHORIZED" &&
                            body.message == "User does not own this pet"
                    }
            }
            .verifyComplete()
    }

    @Test
    fun `should return 400 when use case throws ValidationException in generatePresignedUrl`() {
        // Arrange
        val userId = "user-123"
        val petId = "pet-123"
        val request = GeneratePresignedUrlRequest(contentType = "invalid/type")

        whenever(serverRequest.pathVariable("petId")).thenReturn(petId)
        whenever(serverRequest.bodyToMono(GeneratePresignedUrlRequest::class.java))
            .thenReturn(Mono.just(request))
        whenever(serverRequest.attribute("userId")).thenReturn(Optional.of(userId))
        whenever(generateAvatarPresignedUrlUseCase.execute(any()))
            .thenReturn(Mono.error(ValidationException("Invalid content type")))

        // Act & Assert
        StepVerifier.create(petHandler.generatePresignedUrl(serverRequest))
            .expectNextMatches { response ->
                response.statusCode() == HttpStatus.BAD_REQUEST &&
                    (response as EntityResponse<*>).let { entityResponse ->
                        val body = entityResponse.entity() as ErrorResponse
                        body.error == "VALIDATION_ERROR" &&
                            body.message == "Invalid content type"
                    }
            }
            .verifyComplete()
    }

    // ==================== confirmAvatarUpload Tests ====================

    @Test
    fun `should return 200 with PetResponse including photoUrl when avatar upload is confirmed`() {
        // Arrange
        val userId = "user-123"
        val petId = "pet-123"
        val request = ConfirmAvatarUploadRequest(contentType = "image/jpeg")

        val updatedPet = Pet(
            id = petId,
            name = "Buddy",
            species = Species.DOG,
            breed = "Golden Retriever",
            age = 3,
            birthdate = LocalDate.of(2021, 1, 15),
            weight = BigDecimal("25.5"),
            nickname = "Bud",
            owner = userId,
            registrationDate = LocalDate.now(),
            photoUrl = "https://s3.amazonaws.com/bucket/pets/user-123/pet-123/avatar.jpg"
        )

        whenever(serverRequest.pathVariable("petId")).thenReturn(petId)
        whenever(serverRequest.bodyToMono(ConfirmAvatarUploadRequest::class.java))
            .thenReturn(Mono.just(request))
        whenever(serverRequest.attribute("userId")).thenReturn(Optional.of(userId))
        whenever(confirmAvatarUploadUseCase.execute(any()))
            .thenReturn(Mono.just(updatedPet))

        // Act & Assert
        StepVerifier.create(petHandler.confirmAvatarUpload(serverRequest))
            .expectNextMatches { response ->
                response.statusCode() == HttpStatus.OK &&
                    (response as EntityResponse<*>).let { entityResponse ->
                        val body = entityResponse.entity() as PetResponse
                        body.id == petId &&
                            body.name == "Buddy" &&
                            body.photoUrl == "https://s3.amazonaws.com/bucket/pets/user-123/pet-123/avatar.jpg"
                    }
            }
            .verifyComplete()
    }

    @Test
    fun `should return 401 when no authentication context is found for confirmAvatarUpload`() {
        // Arrange
        val petId = "pet-123"
        whenever(serverRequest.pathVariable("petId")).thenReturn(petId)
        whenever(serverRequest.attribute("userId")).thenReturn(Optional.empty())

        // Act & Assert
        StepVerifier.create(petHandler.confirmAvatarUpload(serverRequest))
            .expectNextMatches { response ->
                response.statusCode() == HttpStatus.UNAUTHORIZED &&
                    (response as EntityResponse<*>).let { entityResponse ->
                        val body = entityResponse.entity() as ErrorResponse
                        body.error == "UNAUTHORIZED" &&
                            body.message == "No authentication found"
                    }
            }
            .verifyComplete()
    }

    @Test
    fun `should return 404 when use case throws PetNotFoundException in confirmAvatarUpload`() {
        // Arrange
        val userId = "user-123"
        val petId = "pet-999"
        val request = ConfirmAvatarUploadRequest(contentType = "image/jpeg")

        whenever(serverRequest.pathVariable("petId")).thenReturn(petId)
        whenever(serverRequest.bodyToMono(ConfirmAvatarUploadRequest::class.java))
            .thenReturn(Mono.just(request))
        whenever(serverRequest.attribute("userId")).thenReturn(Optional.of(userId))
        whenever(confirmAvatarUploadUseCase.execute(any()))
            .thenReturn(Mono.error(PetNotFoundException(petId)))

        // Act & Assert
        StepVerifier.create(petHandler.confirmAvatarUpload(serverRequest))
            .expectNextMatches { response ->
                response.statusCode() == HttpStatus.NOT_FOUND &&
                    (response as EntityResponse<*>).let { entityResponse ->
                        val body = entityResponse.entity() as ErrorResponse
                        body.error == "PET_NOT_FOUND"
                    }
            }
            .verifyComplete()
    }

    @Test
    fun `should return 404 when use case throws PhotoNotFoundException in confirmAvatarUpload`() {
        // Arrange
        val userId = "user-123"
        val petId = "pet-123"
        val photoKey = "pets/user-123/pet-123/avatar.jpg"
        val request = ConfirmAvatarUploadRequest(contentType = "image/jpeg")

        whenever(serverRequest.pathVariable("petId")).thenReturn(petId)
        whenever(serverRequest.bodyToMono(ConfirmAvatarUploadRequest::class.java))
            .thenReturn(Mono.just(request))
        whenever(serverRequest.attribute("userId")).thenReturn(Optional.of(userId))
        whenever(confirmAvatarUploadUseCase.execute(any()))
            .thenReturn(Mono.error(PhotoNotFoundException(photoKey)))

        // Act & Assert
        StepVerifier.create(petHandler.confirmAvatarUpload(serverRequest))
            .expectNextMatches { response ->
                response.statusCode() == HttpStatus.NOT_FOUND &&
                    (response as EntityResponse<*>).let { entityResponse ->
                        val body = entityResponse.entity() as ErrorResponse
                        body.error == "PHOTO_NOT_FOUND"
                    }
            }
            .verifyComplete()
    }

    @Test
    fun `should return 401 when use case throws UnauthorizedException in confirmAvatarUpload`() {
        // Arrange
        val userId = "user-123"
        val petId = "pet-456"
        val request = ConfirmAvatarUploadRequest(contentType = "image/jpeg")

        whenever(serverRequest.pathVariable("petId")).thenReturn(petId)
        whenever(serverRequest.bodyToMono(ConfirmAvatarUploadRequest::class.java))
            .thenReturn(Mono.just(request))
        whenever(serverRequest.attribute("userId")).thenReturn(Optional.of(userId))
        whenever(confirmAvatarUploadUseCase.execute(any()))
            .thenReturn(Mono.error(UnauthorizedException("User does not own this pet")))

        // Act & Assert
        StepVerifier.create(petHandler.confirmAvatarUpload(serverRequest))
            .expectNextMatches { response ->
                response.statusCode() == HttpStatus.UNAUTHORIZED &&
                    (response as EntityResponse<*>).let { entityResponse ->
                        val body = entityResponse.entity() as ErrorResponse
                        body.error == "UNAUTHORIZED" &&
                            body.message == "User does not own this pet"
                    }
            }
            .verifyComplete()
    }
}
