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
import com.donperry.rest.pet.dto.UpdatePetRequest
import com.donperry.usecase.pet.ConfirmAvatarUploadUseCase
import com.donperry.usecase.pet.DeletePetUseCase
import com.donperry.usecase.pet.GenerateAvatarPresignedUrlUseCase
import com.donperry.usecase.pet.GetPetByIdUseCase
import com.donperry.usecase.pet.ListPetsUseCase
import com.donperry.usecase.pet.RegisterPetUseCase
import com.donperry.usecase.pet.UpdatePetUseCase
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
    private lateinit var updatePetUseCase: UpdatePetUseCase

    @Mock
    private lateinit var deletePetUseCase: DeletePetUseCase

    @Mock
    private lateinit var listPetsUseCase: ListPetsUseCase

    @Mock
    private lateinit var getPetByIdUseCase: GetPetByIdUseCase

    @Mock
    private lateinit var serverRequest: ServerRequest

    @InjectMocks
    private lateinit var petHandler: PetHandler


    @Test
    fun `should return 201 with PetResponse when pet registration is successful`() {
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
        whenever(serverRequest.attribute("userId")).thenReturn(Optional.empty())

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


    @Test
    fun `should return 200 with PresignedUrlResponse when URL generation is successful`() {
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
        val petId = "pet-123"
        whenever(serverRequest.pathVariable("petId")).thenReturn(petId)
        whenever(serverRequest.attribute("userId")).thenReturn(Optional.empty())

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
        val userId = "user-123"
        val petId = "pet-999"
        val request = GeneratePresignedUrlRequest(contentType = "image/jpeg")

        whenever(serverRequest.pathVariable("petId")).thenReturn(petId)
        whenever(serverRequest.bodyToMono(GeneratePresignedUrlRequest::class.java))
            .thenReturn(Mono.just(request))
        whenever(serverRequest.attribute("userId")).thenReturn(Optional.of(userId))
        whenever(generateAvatarPresignedUrlUseCase.execute(any()))
            .thenReturn(Mono.error(PetNotFoundException(petId)))

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
        val userId = "user-123"
        val petId = "pet-456"
        val request = GeneratePresignedUrlRequest(contentType = "image/jpeg")

        whenever(serverRequest.pathVariable("petId")).thenReturn(petId)
        whenever(serverRequest.bodyToMono(GeneratePresignedUrlRequest::class.java))
            .thenReturn(Mono.just(request))
        whenever(serverRequest.attribute("userId")).thenReturn(Optional.of(userId))
        whenever(generateAvatarPresignedUrlUseCase.execute(any()))
            .thenReturn(Mono.error(UnauthorizedException("User does not own this pet")))

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
        val userId = "user-123"
        val petId = "pet-123"
        val request = GeneratePresignedUrlRequest(contentType = "invalid/type")

        whenever(serverRequest.pathVariable("petId")).thenReturn(petId)
        whenever(serverRequest.bodyToMono(GeneratePresignedUrlRequest::class.java))
            .thenReturn(Mono.just(request))
        whenever(serverRequest.attribute("userId")).thenReturn(Optional.of(userId))
        whenever(generateAvatarPresignedUrlUseCase.execute(any()))
            .thenReturn(Mono.error(ValidationException("Invalid content type")))

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


    @Test
    fun `should return 200 with PetResponse including photoUrl when avatar upload is confirmed`() {
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
        val petId = "pet-123"
        whenever(serverRequest.pathVariable("petId")).thenReturn(petId)
        whenever(serverRequest.attribute("userId")).thenReturn(Optional.empty())

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
        val userId = "user-123"
        val petId = "pet-999"
        val request = ConfirmAvatarUploadRequest(contentType = "image/jpeg")

        whenever(serverRequest.pathVariable("petId")).thenReturn(petId)
        whenever(serverRequest.bodyToMono(ConfirmAvatarUploadRequest::class.java))
            .thenReturn(Mono.just(request))
        whenever(serverRequest.attribute("userId")).thenReturn(Optional.of(userId))
        whenever(confirmAvatarUploadUseCase.execute(any()))
            .thenReturn(Mono.error(PetNotFoundException(petId)))

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
        val userId = "user-123"
        val petId = "pet-456"
        val request = ConfirmAvatarUploadRequest(contentType = "image/jpeg")

        whenever(serverRequest.pathVariable("petId")).thenReturn(petId)
        whenever(serverRequest.bodyToMono(ConfirmAvatarUploadRequest::class.java))
            .thenReturn(Mono.just(request))
        whenever(serverRequest.attribute("userId")).thenReturn(Optional.of(userId))
        whenever(confirmAvatarUploadUseCase.execute(any()))
            .thenReturn(Mono.error(UnauthorizedException("User does not own this pet")))

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


    @Test
    fun `should return 200 with PetResponse when pet update is successful`() {
        val userId = "user-123"
        val petId = "pet-123"
        val request = UpdatePetRequest(
            name = "Buddy Updated",
            species = "CAT",
            breed = "Persian",
            age = 4,
            birthdate = LocalDate.of(2020, 5, 10),
            weight = BigDecimal("30.0"),
            nickname = "Buddy Bear",
        )

        val updatedPet = Pet(
            id = petId,
            name = "Buddy Updated",
            species = Species.CAT,
            breed = "Persian",
            age = 4,
            birthdate = LocalDate.of(2020, 5, 10),
            weight = BigDecimal("30.0"),
            nickname = "Buddy Bear",
            owner = userId,
            registrationDate = LocalDate.of(2023, 6, 1),
        )

        whenever(serverRequest.pathVariable("petId")).thenReturn(petId)
        whenever(serverRequest.bodyToMono(UpdatePetRequest::class.java))
            .thenReturn(Mono.just(request))
        whenever(serverRequest.attribute("userId")).thenReturn(Optional.of(userId))
        whenever(updatePetUseCase.execute(any())).thenReturn(Mono.just(updatedPet))

        StepVerifier.create(petHandler.updatePet(serverRequest))
            .expectNextMatches { response ->
                response.statusCode() == HttpStatus.OK &&
                    (response as EntityResponse<*>).let { entityResponse ->
                        val body = entityResponse.entity() as PetResponse
                        body.id == petId &&
                            body.name == "Buddy Updated" &&
                            body.species == "CAT" &&
                            body.breed == "Persian" &&
                            body.age == 4 &&
                            body.owner == userId &&
                            body.photoUrl == null
                    }
            }
            .verifyComplete()
    }

    @Test
    fun `should return 400 when update validation fails for blank name`() {
        val userId = "user-123"
        val petId = "pet-123"
        val request = UpdatePetRequest(
            name = "",
            species = "DOG",
            breed = "Golden Retriever",
            age = 3,
            birthdate = null,
            weight = null,
            nickname = null,
        )

        whenever(serverRequest.pathVariable("petId")).thenReturn(petId)
        whenever(serverRequest.bodyToMono(UpdatePetRequest::class.java))
            .thenReturn(Mono.just(request))
        whenever(serverRequest.attribute("userId")).thenReturn(Optional.of(userId))

        StepVerifier.create(petHandler.updatePet(serverRequest))
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
    fun `should return 400 when update validation fails for invalid species`() {
        val userId = "user-123"
        val petId = "pet-123"
        val request = UpdatePetRequest(
            name = "Buddy",
            species = "LIZARD",
            breed = null,
            age = 3,
            birthdate = null,
            weight = null,
            nickname = null,
        )

        whenever(serverRequest.pathVariable("petId")).thenReturn(petId)
        whenever(serverRequest.bodyToMono(UpdatePetRequest::class.java))
            .thenReturn(Mono.just(request))
        whenever(serverRequest.attribute("userId")).thenReturn(Optional.of(userId))

        StepVerifier.create(petHandler.updatePet(serverRequest))
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
    fun `should return 401 when user is not owner in update`() {
        val userId = "user-123"
        val petId = "pet-456"
        val request = UpdatePetRequest(
            name = "Buddy",
            species = "DOG",
            breed = "Golden Retriever",
            age = 3,
            birthdate = null,
            weight = null,
            nickname = null,
        )

        whenever(serverRequest.pathVariable("petId")).thenReturn(petId)
        whenever(serverRequest.bodyToMono(UpdatePetRequest::class.java))
            .thenReturn(Mono.just(request))
        whenever(serverRequest.attribute("userId")).thenReturn(Optional.of(userId))
        whenever(updatePetUseCase.execute(any()))
            .thenReturn(Mono.error(UnauthorizedException("User does not own this pet")))

        StepVerifier.create(petHandler.updatePet(serverRequest))
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
    fun `should return 404 when pet not found in update`() {
        val userId = "user-123"
        val petId = "pet-999"
        val request = UpdatePetRequest(
            name = "Buddy",
            species = "DOG",
            breed = "Golden Retriever",
            age = 3,
            birthdate = null,
            weight = null,
            nickname = null,
        )

        whenever(serverRequest.pathVariable("petId")).thenReturn(petId)
        whenever(serverRequest.bodyToMono(UpdatePetRequest::class.java))
            .thenReturn(Mono.just(request))
        whenever(serverRequest.attribute("userId")).thenReturn(Optional.of(userId))
        whenever(updatePetUseCase.execute(any()))
            .thenReturn(Mono.error(PetNotFoundException(petId)))

        StepVerifier.create(petHandler.updatePet(serverRequest))
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
    fun `should return 401 when no authentication context is found for updatePet`() {
        val petId = "pet-123"
        whenever(serverRequest.pathVariable("petId")).thenReturn(petId)
        whenever(serverRequest.attribute("userId")).thenReturn(Optional.empty())

        StepVerifier.create(petHandler.updatePet(serverRequest))
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
    fun `should return 204 when pet deletion is successful`() {
        val userId = "user-123"
        val petId = "pet-123"

        whenever(serverRequest.pathVariable("petId")).thenReturn(petId)
        whenever(serverRequest.attribute("userId")).thenReturn(Optional.of(userId))
        whenever(deletePetUseCase.execute(any())).thenReturn(Mono.empty())

        StepVerifier.create(petHandler.deletePet(serverRequest))
            .expectNextMatches { response ->
                response.statusCode() == HttpStatus.NO_CONTENT
            }
            .verifyComplete()
    }

    @Test
    fun `should return 404 when pet not found in delete`() {
        val userId = "user-123"
        val petId = "pet-999"

        whenever(serverRequest.pathVariable("petId")).thenReturn(petId)
        whenever(serverRequest.attribute("userId")).thenReturn(Optional.of(userId))
        whenever(deletePetUseCase.execute(any()))
            .thenReturn(Mono.error(PetNotFoundException(petId)))

        StepVerifier.create(petHandler.deletePet(serverRequest))
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
    fun `should return 401 when no authentication context is found for deletePet`() {
        val petId = "pet-123"
        whenever(serverRequest.pathVariable("petId")).thenReturn(petId)
        whenever(serverRequest.attribute("userId")).thenReturn(Optional.empty())

        StepVerifier.create(petHandler.deletePet(serverRequest))
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
    fun `should return 500 when use case throws unexpected RuntimeException in delete`() {
        val userId = "user-123"
        val petId = "pet-123"

        whenever(serverRequest.pathVariable("petId")).thenReturn(petId)
        whenever(serverRequest.attribute("userId")).thenReturn(Optional.of(userId))
        whenever(deletePetUseCase.execute(any()))
            .thenReturn(Mono.error(RuntimeException("Unexpected database error")))

        StepVerifier.create(petHandler.deletePet(serverRequest))
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

    @Test
    fun `should return 204 when deleting already deleted pet (idempotent)`() {
        val userId = "user-123"
        val petId = "pet-123"

        whenever(serverRequest.pathVariable("petId")).thenReturn(petId)
        whenever(serverRequest.attribute("userId")).thenReturn(Optional.of(userId))
        whenever(deletePetUseCase.execute(any())).thenReturn(Mono.empty())

        StepVerifier.create(petHandler.deletePet(serverRequest))
            .expectNextMatches { response ->
                response.statusCode() == HttpStatus.NO_CONTENT
            }
            .verifyComplete()
    }


    @Test
    fun `should return 200 with list of pets when user has multiple pets`() {
        val userId = "user-123"
        val pet1 = Pet(
            id = "pet-1",
            name = "Buddy",
            species = Species.DOG,
            breed = "Golden Retriever",
            age = 3,
            owner = userId,
            registrationDate = LocalDate.of(2023, 1, 15),
            photoUrl = "https://example.com/buddy.jpg"
        )
        val pet2 = Pet(
            id = "pet-2",
            name = "Mittens",
            species = Species.CAT,
            breed = "Persian",
            age = 2,
            owner = userId,
            registrationDate = LocalDate.of(2023, 6, 20),
            photoUrl = null
        )

        whenever(serverRequest.attribute("userId")).thenReturn(Optional.of(userId))
        whenever(listPetsUseCase.execute(userId))
            .thenReturn(reactor.core.publisher.Flux.just(pet1, pet2))

        StepVerifier.create(petHandler.listPets(serverRequest))
            .expectNextMatches { response ->
                response.statusCode() == HttpStatus.OK &&
                    (response as EntityResponse<*>).let { entityResponse ->
                        val body = entityResponse.entity() as List<*>
                        body.size == 2 &&
                            (body[0] as com.donperry.rest.pet.dto.PetListResponse).let { dto ->
                                dto.id == "pet-1" &&
                                    dto.name == "Buddy" &&
                                    dto.species == "DOG" &&
                                    dto.breed == "Golden Retriever" &&
                                    dto.photoUrl == "https://example.com/buddy.jpg"
                            } &&
                            (body[1] as com.donperry.rest.pet.dto.PetListResponse).let { dto ->
                                dto.id == "pet-2" &&
                                    dto.name == "Mittens" &&
                                    dto.species == "CAT" &&
                                    dto.breed == "Persian" &&
                                    dto.photoUrl == null
                            }
                    }
            }
            .verifyComplete()
    }

    @Test
    fun `should return 200 with empty list when user has no pets`() {
        val userId = "user-no-pets"

        whenever(serverRequest.attribute("userId")).thenReturn(Optional.of(userId))
        whenever(listPetsUseCase.execute(userId))
            .thenReturn(reactor.core.publisher.Flux.empty())

        StepVerifier.create(petHandler.listPets(serverRequest))
            .expectNextMatches { response ->
                response.statusCode() == HttpStatus.OK &&
                    (response as EntityResponse<*>).let { entityResponse ->
                        val body = entityResponse.entity() as List<*>
                        body.isEmpty()
                    }
            }
            .verifyComplete()
    }

    @Test
    fun `should return 200 with single pet when user has one pet`() {
        val userId = "user-456"
        val pet = Pet(
            id = "pet-solo",
            name = "Rex",
            species = Species.DOG,
            breed = null,
            age = 1,
            owner = userId,
            registrationDate = LocalDate.now(),
            photoUrl = null
        )

        whenever(serverRequest.attribute("userId")).thenReturn(Optional.of(userId))
        whenever(listPetsUseCase.execute(userId))
            .thenReturn(reactor.core.publisher.Flux.just(pet))

        StepVerifier.create(petHandler.listPets(serverRequest))
            .expectNextMatches { response ->
                response.statusCode() == HttpStatus.OK &&
                    (response as EntityResponse<*>).let { entityResponse ->
                        val body = entityResponse.entity() as List<*>
                        body.size == 1 &&
                            (body[0] as com.donperry.rest.pet.dto.PetListResponse).let { dto ->
                                dto.id == "pet-solo" &&
                                    dto.name == "Rex" &&
                                    dto.species == "DOG" &&
                                    dto.breed == null &&
                                    dto.photoUrl == null
                            }
                    }
            }
            .verifyComplete()
    }

    @Test
    fun `should map Pet to PetListResponse correctly with all species`() {
        val userId = "user-variety"
        val dog1 = Pet(
            id = "pet-dog-1",
            name = "Max",
            species = Species.DOG,
            breed = "Labrador",
            age = 4,
            owner = userId,
            registrationDate = LocalDate.now(),
            photoUrl = "https://example.com/max.jpg"
        )
        val cat1 = Pet(
            id = "pet-cat-1",
            name = "Whiskers",
            species = Species.CAT,
            breed = "Siamese",
            age = 2,
            owner = userId,
            registrationDate = LocalDate.now(),
            photoUrl = null
        )
        val dog2 = Pet(
            id = "pet-dog-2",
            name = "Buddy",
            species = Species.DOG,
            breed = "Beagle",
            age = 3,
            owner = userId,
            registrationDate = LocalDate.now(),
            photoUrl = null
        )
        val cat2 = Pet(
            id = "pet-cat-2",
            name = "Mittens",
            species = Species.CAT,
            breed = "Persian",
            age = 1,
            owner = userId,
            registrationDate = LocalDate.now(),
            photoUrl = null
        )

        whenever(serverRequest.attribute("userId")).thenReturn(Optional.of(userId))
        whenever(listPetsUseCase.execute(userId))
            .thenReturn(reactor.core.publisher.Flux.just(dog1, cat1, dog2, cat2))

        StepVerifier.create(petHandler.listPets(serverRequest))
            .expectNextMatches { response ->
                response.statusCode() == HttpStatus.OK &&
                    (response as EntityResponse<*>).let { entityResponse ->
                        val body = entityResponse.entity() as List<*>
                        body.size == 4 &&
                            (body[0] as com.donperry.rest.pet.dto.PetListResponse).species == "DOG" &&
                            (body[1] as com.donperry.rest.pet.dto.PetListResponse).species == "CAT" &&
                            (body[2] as com.donperry.rest.pet.dto.PetListResponse).species == "DOG" &&
                            (body[3] as com.donperry.rest.pet.dto.PetListResponse).species == "CAT"
                    }
            }
            .verifyComplete()
    }

    @Test
    fun `should return 401 when no authentication context is found for listPets`() {
        whenever(serverRequest.attribute("userId")).thenReturn(Optional.empty())

        StepVerifier.create(petHandler.listPets(serverRequest))
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
    fun `should return 500 when use case throws unexpected RuntimeException in listPets`() {
        val userId = "user-error"

        whenever(serverRequest.attribute("userId")).thenReturn(Optional.of(userId))
        whenever(listPetsUseCase.execute(userId))
            .thenReturn(reactor.core.publisher.Flux.error(RuntimeException("Database connection failed")))

        StepVerifier.create(petHandler.listPets(serverRequest))
            .expectNextMatches { response ->
                response.statusCode() == HttpStatus.INTERNAL_SERVER_ERROR &&
                    (response as EntityResponse<*>).let { entityResponse ->
                        val body = entityResponse.entity() as ErrorResponse
                        body.error == "INTERNAL_ERROR" &&
                            body.message == "Database connection failed"
                    }
            }
            .verifyComplete()
    }

    @Test
    fun `should only include required fields in PetListResponse`() {
        val userId = "user-full"
        val pet = Pet(
            id = "pet-123",
            name = "Buddy",
            species = Species.DOG,
            breed = "Golden Retriever",
            age = 3,
            birthdate = LocalDate.of(2021, 1, 15),
            weight = BigDecimal("25.5"),
            nickname = "Bud",
            owner = userId,
            registrationDate = LocalDate.of(2023, 6, 1),
            photoUrl = "https://example.com/buddy.jpg"
        )

        whenever(serverRequest.attribute("userId")).thenReturn(Optional.of(userId))
        whenever(listPetsUseCase.execute(userId))
            .thenReturn(reactor.core.publisher.Flux.just(pet))

        StepVerifier.create(petHandler.listPets(serverRequest))
            .expectNextMatches { response ->
                response.statusCode() == HttpStatus.OK &&
                    (response as EntityResponse<*>).let { entityResponse ->
                        val body = entityResponse.entity() as List<*>
                        body.size == 1 &&
                            (body[0] as com.donperry.rest.pet.dto.PetListResponse).let { dto ->
                                dto.id == "pet-123" &&
                                    dto.name == "Buddy" &&
                                    dto.species == "DOG" &&
                                    dto.breed == "Golden Retriever" &&
                                    dto.photoUrl == "https://example.com/buddy.jpg"
                            }
                    }
            }
            .verifyComplete()
    }

    @Test
    fun `should handle maximum allowed pets (10 pets) in listPets`() {
        val userId = "user-max"
        val pets = (1..10).map { index ->
            Pet(
                id = "pet-$index",
                name = "Pet $index",
                species = Species.DOG,
                breed = "Mixed",
                age = index,
                owner = userId,
                registrationDate = LocalDate.now(),
                photoUrl = null
            )
        }

        whenever(serverRequest.attribute("userId")).thenReturn(Optional.of(userId))
        whenever(listPetsUseCase.execute(userId))
            .thenReturn(reactor.core.publisher.Flux.fromIterable(pets))

        StepVerifier.create(petHandler.listPets(serverRequest))
            .expectNextMatches { response ->
                response.statusCode() == HttpStatus.OK &&
                    (response as EntityResponse<*>).let { entityResponse ->
                        val body = entityResponse.entity() as List<*>
                        body.size == 10
                    }
            }
            .verifyComplete()
    }


    @Test
    fun `should return 200 with PetDetailResponse when pet is found and user is owner`() {
        val userId = "user-123"
        val petId = "pet-123"
        val expectedPet = Pet(
            id = petId,
            name = "Buddy",
            species = Species.DOG,
            breed = "Golden Retriever",
            age = 3,
            birthdate = LocalDate.of(2021, 1, 15),
            weight = BigDecimal("25.5"),
            nickname = "Bud",
            owner = userId,
            registrationDate = LocalDate.of(2023, 6, 1),
            photoUrl = "https://example.com/photo.jpg"
        )

        whenever(serverRequest.pathVariable("petId")).thenReturn(petId)
        whenever(serverRequest.attribute("userId")).thenReturn(Optional.of(userId))
        whenever(getPetByIdUseCase.execute(petId, userId)).thenReturn(Mono.just(expectedPet))

        StepVerifier.create(petHandler.getPetDetail(serverRequest))
            .expectNextMatches { response ->
                response.statusCode() == HttpStatus.OK &&
                    (response as EntityResponse<*>).let { entityResponse ->
                        val body = entityResponse.entity() as com.donperry.rest.pet.dto.PetDetailResponse
                        body.id == petId &&
                            body.name == "Buddy" &&
                            body.species == "DOG" &&
                            body.breed == "Golden Retriever" &&
                            body.age == 3 &&
                            body.birthdate == LocalDate.of(2021, 1, 15) &&
                            body.weight == BigDecimal("25.5") &&
                            body.nickname == "Bud" &&
                            body.owner == userId &&
                            body.registrationDate == LocalDate.of(2023, 6, 1) &&
                            body.photoUrl == "https://example.com/photo.jpg"
                    }
            }
            .verifyComplete()
    }

    @Test
    fun `should return 200 with PetDetailResponse with minimal fields when pet has no optional data`() {
        val userId = "user-456"
        val petId = "pet-456"
        val expectedPet = Pet(
            id = petId,
            name = "Rex",
            species = Species.CAT,
            breed = null,
            age = 1,
            owner = userId,
            registrationDate = LocalDate.now(),
            photoUrl = null
        )

        whenever(serverRequest.pathVariable("petId")).thenReturn(petId)
        whenever(serverRequest.attribute("userId")).thenReturn(Optional.of(userId))
        whenever(getPetByIdUseCase.execute(petId, userId)).thenReturn(Mono.just(expectedPet))

        StepVerifier.create(petHandler.getPetDetail(serverRequest))
            .expectNextMatches { response ->
                response.statusCode() == HttpStatus.OK &&
                    (response as EntityResponse<*>).let { entityResponse ->
                        val body = entityResponse.entity() as com.donperry.rest.pet.dto.PetDetailResponse
                        body.id == petId &&
                            body.name == "Rex" &&
                            body.species == "CAT" &&
                            body.breed == null &&
                            body.age == 1 &&
                            body.birthdate == null &&
                            body.weight == null &&
                            body.nickname == null &&
                            body.owner == userId &&
                            body.photoUrl == null
                    }
            }
            .verifyComplete()
    }

    @Test
    fun `should return 404 when pet not found in getPetDetail`() {
        val userId = "user-123"
        val petId = "pet-999"

        whenever(serverRequest.pathVariable("petId")).thenReturn(petId)
        whenever(serverRequest.attribute("userId")).thenReturn(Optional.of(userId))
        whenever(getPetByIdUseCase.execute(petId, userId))
            .thenReturn(Mono.error(PetNotFoundException(petId)))

        StepVerifier.create(petHandler.getPetDetail(serverRequest))
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
    fun `should return 404 when pet is soft-deleted in getPetDetail`() {
        val userId = "user-123"
        val petId = "pet-deleted"

        whenever(serverRequest.pathVariable("petId")).thenReturn(petId)
        whenever(serverRequest.attribute("userId")).thenReturn(Optional.of(userId))
        whenever(getPetByIdUseCase.execute(petId, userId))
            .thenReturn(Mono.error(PetNotFoundException(petId)))

        StepVerifier.create(petHandler.getPetDetail(serverRequest))
            .expectNextMatches { response ->
                response.statusCode() == HttpStatus.NOT_FOUND &&
                    (response as EntityResponse<*>).let { entityResponse ->
                        val body = entityResponse.entity() as ErrorResponse
                        body.error == "PET_NOT_FOUND" &&
                            body.message == "Pet with id $petId not found"
                    }
            }
            .verifyComplete()
    }

    @Test
    fun `should return 401 when user is not owner in getPetDetail`() {
        val userId = "user-123"
        val petId = "pet-456"

        whenever(serverRequest.pathVariable("petId")).thenReturn(petId)
        whenever(serverRequest.attribute("userId")).thenReturn(Optional.of(userId))
        whenever(getPetByIdUseCase.execute(petId, userId))
            .thenReturn(Mono.error(UnauthorizedException("Not authorized to view this pet")))

        StepVerifier.create(petHandler.getPetDetail(serverRequest))
            .expectNextMatches { response ->
                response.statusCode() == HttpStatus.UNAUTHORIZED &&
                    (response as EntityResponse<*>).let { entityResponse ->
                        val body = entityResponse.entity() as ErrorResponse
                        body.error == "UNAUTHORIZED" &&
                            body.message == "Not authorized to view this pet"
                    }
            }
            .verifyComplete()
    }

    @Test
    fun `should return 401 when no authentication context is found for getPetDetail`() {
        val petId = "pet-123"
        whenever(serverRequest.pathVariable("petId")).thenReturn(petId)
        whenever(serverRequest.attribute("userId")).thenReturn(Optional.empty())

        StepVerifier.create(petHandler.getPetDetail(serverRequest))
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
    fun `should return 500 when use case throws unexpected RuntimeException in getPetDetail`() {
        val userId = "user-123"
        val petId = "pet-123"

        whenever(serverRequest.pathVariable("petId")).thenReturn(petId)
        whenever(serverRequest.attribute("userId")).thenReturn(Optional.of(userId))
        whenever(getPetByIdUseCase.execute(petId, userId))
            .thenReturn(Mono.error(RuntimeException("Database connection failed")))

        StepVerifier.create(petHandler.getPetDetail(serverRequest))
            .expectNextMatches { response ->
                response.statusCode() == HttpStatus.INTERNAL_SERVER_ERROR &&
                    (response as EntityResponse<*>).let { entityResponse ->
                        val body = entityResponse.entity() as ErrorResponse
                        body.error == "INTERNAL_ERROR" &&
                            body.message == "Database connection failed"
                    }
            }
            .verifyComplete()
    }

    @Test
    fun `should map all species correctly in getPetDetail`() {
        val userId = "user-multi"

        val dogPet = Pet(
            id = "pet-dog",
            name = "Max",
            species = Species.DOG,
            breed = "Labrador",
            age = 4,
            owner = userId,
            registrationDate = LocalDate.of(2022, 1, 10),
            photoUrl = "https://example.com/max.jpg"
        )

        whenever(serverRequest.pathVariable("petId")).thenReturn("pet-dog")
        whenever(serverRequest.attribute("userId")).thenReturn(Optional.of(userId))
        whenever(getPetByIdUseCase.execute("pet-dog", userId)).thenReturn(Mono.just(dogPet))

        StepVerifier.create(petHandler.getPetDetail(serverRequest))
            .expectNextMatches { response ->
                response.statusCode() == HttpStatus.OK &&
                    (response as EntityResponse<*>).let { entityResponse ->
                        val body = entityResponse.entity() as com.donperry.rest.pet.dto.PetDetailResponse
                        body.species == "DOG"
                    }
            }
            .verifyComplete()
    }

    @Test
    fun `should return pet with zero age in getPetDetail`() {
        val userId = "user-new-owner"
        val petId = "pet-puppy"
        val youngPet = Pet(
            id = petId,
            name = "Puppy",
            species = Species.DOG,
            breed = "Mixed",
            age = 0,
            owner = userId,
            registrationDate = LocalDate.now(),
            photoUrl = null
        )

        whenever(serverRequest.pathVariable("petId")).thenReturn(petId)
        whenever(serverRequest.attribute("userId")).thenReturn(Optional.of(userId))
        whenever(getPetByIdUseCase.execute(petId, userId)).thenReturn(Mono.just(youngPet))

        StepVerifier.create(petHandler.getPetDetail(serverRequest))
            .expectNextMatches { response ->
                response.statusCode() == HttpStatus.OK &&
                    (response as EntityResponse<*>).let { entityResponse ->
                        val body = entityResponse.entity() as com.donperry.rest.pet.dto.PetDetailResponse
                        body.age == 0
                    }
            }
            .verifyComplete()
    }
}
