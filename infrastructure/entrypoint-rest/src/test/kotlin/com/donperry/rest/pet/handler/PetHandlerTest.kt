package com.donperry.rest.pet.handler

import com.donperry.model.exception.PetLimitExceededException
import com.donperry.model.exception.PhotoSizeExceededException
import com.donperry.model.exception.PhotoUploadException
import com.donperry.model.exception.UnauthorizedException
import com.donperry.model.exception.ValidationException
import com.donperry.model.pet.Pet
import com.donperry.model.pet.Species
import com.donperry.rest.common.dto.ErrorResponse
import com.donperry.rest.pet.dto.PetResponse
import com.donperry.rest.pet.dto.RegisterPetRequest
import com.donperry.usecase.pet.RegisterPetUseCase
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever
import org.springframework.core.io.buffer.DataBuffer
import org.springframework.core.io.buffer.DefaultDataBufferFactory
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.codec.multipart.FilePart
import org.springframework.http.codec.multipart.Part
import org.springframework.security.authentication.TestingAuthenticationToken
import org.springframework.util.LinkedMultiValueMap
import org.springframework.util.MultiValueMap
import org.springframework.web.reactive.function.server.EntityResponse
import org.springframework.web.reactive.function.server.ServerRequest
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import reactor.test.StepVerifier
import java.math.BigDecimal
import java.time.LocalDate

@ExtendWith(MockitoExtension::class)
class PetHandlerTest {

    @Mock
    private lateinit var registerPetUseCase: RegisterPetUseCase

    @Mock
    private lateinit var serverRequest: ServerRequest

    @InjectMocks
    private lateinit var petHandler: PetHandler

    private val objectMapper = jacksonObjectMapper().apply {
        findAndRegisterModules()
    }

    private val dataBufferFactory = DefaultDataBufferFactory()

    @Test
    fun `should register pet without photo when valid request`() {
        // Arrange
        val userId = "user-123"
        val petRequest = RegisterPetRequest(
            name = "Buddy",
            species = "DOG",
            breed = "Golden Retriever",
            age = 3,
            birthdate = LocalDate.of(2021, 1, 15),
            weight = BigDecimal("25.5"),
            nickname = "Bud"
        )

        val petJson = objectMapper.writeValueAsBytes(petRequest)
        val petPart = createMockPart(petJson)
        val multipartData = LinkedMultiValueMap<String, Part>()
        multipartData.add("pet", petPart)

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

        whenever(serverRequest.multipartData()).thenReturn(Mono.just(multipartData))
        whenever(registerPetUseCase.execute(any())).thenReturn(Mono.just(expectedPet))

        val authentication = TestingAuthenticationToken(userId, null)
        authentication.isAuthenticated = true

        // Act & Assert
        StepVerifier.create(
            petHandler.registerPet(serverRequest)
                .contextWrite(
                    org.springframework.security.core.context.ReactiveSecurityContextHolder
                        .withAuthentication(authentication)
                )
        )
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
    fun `should register pet with photo when photo part provided`() {
        // Arrange
        val userId = "user-123"
        val petRequest = RegisterPetRequest(
            name = "Whiskers",
            species = "CAT",
            breed = "Persian",
            age = 2,
            birthdate = LocalDate.of(2022, 3, 10),
            weight = BigDecimal("4.5"),
            nickname = null
        )

        val petJson = objectMapper.writeValueAsBytes(petRequest)
        val photoBytes = "fake-photo-content".toByteArray()

        val petPart = createMockPart(petJson)
        val photoPart = createMockFilePart("cat.jpg", "image/jpeg", photoBytes)

        val multipartData = LinkedMultiValueMap<String, Part>()
        multipartData.add("pet", petPart)
        multipartData.add("photo", photoPart)

        val expectedPet = Pet(
            id = "pet-456",
            name = "Whiskers",
            species = Species.CAT,
            breed = "Persian",
            age = 2,
            birthdate = LocalDate.of(2022, 3, 10),
            weight = BigDecimal("4.5"),
            nickname = null,
            owner = userId,
            registrationDate = LocalDate.now(),
            photoUrl = "https://s3.amazonaws.com/bucket/cat.jpg"
        )

        whenever(serverRequest.multipartData()).thenReturn(Mono.just(multipartData))
        whenever(registerPetUseCase.execute(any())).thenReturn(Mono.just(expectedPet))

        val authentication = TestingAuthenticationToken(userId, null)
        authentication.isAuthenticated = true

        // Act & Assert
        StepVerifier.create(
            petHandler.registerPet(serverRequest)
                .contextWrite(
                    org.springframework.security.core.context.ReactiveSecurityContextHolder
                        .withAuthentication(authentication)
                )
        )
            .expectNextMatches { response ->
                response.statusCode() == HttpStatus.CREATED &&
                    (response as EntityResponse<*>).let { entityResponse ->
                        val body = entityResponse.entity() as PetResponse
                        body.id == "pet-456" &&
                            body.name == "Whiskers" &&
                            body.species == "CAT" &&
                            body.photoUrl == "https://s3.amazonaws.com/bucket/cat.jpg"
                    }
            }
            .verifyComplete()
    }

    @Test
    fun `should return 400 when pet part is missing in multipart request`() {
        // Arrange
        val userId = "user-123"
        val multipartData = LinkedMultiValueMap<String, Part>()
        // No pet part added

        whenever(serverRequest.multipartData()).thenReturn(Mono.just(multipartData))

        val authentication = TestingAuthenticationToken(userId, null)
        authentication.isAuthenticated = true

        // Act & Assert
        StepVerifier.create(
            petHandler.registerPet(serverRequest)
                .contextWrite(
                    org.springframework.security.core.context.ReactiveSecurityContextHolder
                        .withAuthentication(authentication)
                )
        )
            .expectNextMatches { response ->
                response.statusCode() == HttpStatus.BAD_REQUEST &&
                    (response as EntityResponse<*>).let { entityResponse ->
                        val body = entityResponse.entity() as ErrorResponse
                        body.error == "VALIDATION_ERROR" &&
                            body.message.contains("Missing 'pet' part")
                    }
            }
            .verifyComplete()
    }

    @Test
    fun `should return 400 when species is invalid`() {
        // Arrange
        val userId = "user-123"
        val petRequest = RegisterPetRequest(
            name = "Buddy",
            species = "LIZARD",  // Invalid species
            breed = null,
            age = 3,
            birthdate = null,
            weight = null,
            nickname = null
        )

        val petJson = objectMapper.writeValueAsBytes(petRequest)
        val petPart = createMockPart(petJson)
        val multipartData = LinkedMultiValueMap<String, Part>()
        multipartData.add("pet", petPart)

        whenever(serverRequest.multipartData()).thenReturn(Mono.just(multipartData))

        val authentication = TestingAuthenticationToken(userId, null)
        authentication.isAuthenticated = true

        // Act & Assert
        StepVerifier.create(
            petHandler.registerPet(serverRequest)
                .contextWrite(
                    org.springframework.security.core.context.ReactiveSecurityContextHolder
                        .withAuthentication(authentication)
                )
        )
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
    fun `should return 401 when no authentication context found`() {
        // Arrange - no authentication context provided

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
    fun `should return 400 when use case throws ValidationException`() {
        // Arrange
        val userId = "user-123"
        val petRequest = RegisterPetRequest(
            name = "",  // Invalid name
            species = "DOG",
            breed = null,
            age = 3,
            birthdate = null,
            weight = null,
            nickname = null
        )

        val petJson = objectMapper.writeValueAsBytes(petRequest)
        val petPart = createMockPart(petJson)
        val multipartData = LinkedMultiValueMap<String, Part>()
        multipartData.add("pet", petPart)

        whenever(serverRequest.multipartData()).thenReturn(Mono.just(multipartData))
        whenever(registerPetUseCase.execute(any())).thenReturn(
            Mono.error(ValidationException("Pet name cannot be empty"))
        )

        val authentication = TestingAuthenticationToken(userId, null)
        authentication.isAuthenticated = true

        // Act & Assert
        StepVerifier.create(
            petHandler.registerPet(serverRequest)
                .contextWrite(
                    org.springframework.security.core.context.ReactiveSecurityContextHolder
                        .withAuthentication(authentication)
                )
        )
            .expectNextMatches { response ->
                response.statusCode() == HttpStatus.BAD_REQUEST &&
                    (response as EntityResponse<*>).let { entityResponse ->
                        val body = entityResponse.entity() as ErrorResponse
                        body.error == "VALIDATION_ERROR" &&
                            body.message == "Pet name cannot be empty"
                    }
            }
            .verifyComplete()
    }

    @Test
    fun `should return 409 when use case throws PetLimitExceededException`() {
        // Arrange
        val userId = "user-123"
        val petRequest = RegisterPetRequest(
            name = "Buddy",
            species = "DOG",
            breed = null,
            age = 3,
            birthdate = null,
            weight = null,
            nickname = null
        )

        val petJson = objectMapper.writeValueAsBytes(petRequest)
        val petPart = createMockPart(petJson)
        val multipartData = LinkedMultiValueMap<String, Part>()
        multipartData.add("pet", petPart)

        whenever(serverRequest.multipartData()).thenReturn(Mono.just(multipartData))
        whenever(registerPetUseCase.execute(any())).thenReturn(
            Mono.error(PetLimitExceededException(userId))
        )

        val authentication = TestingAuthenticationToken(userId, null)
        authentication.isAuthenticated = true

        // Act & Assert
        StepVerifier.create(
            petHandler.registerPet(serverRequest)
                .contextWrite(
                    org.springframework.security.core.context.ReactiveSecurityContextHolder
                        .withAuthentication(authentication)
                )
        )
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
    fun `should return 413 when use case throws PhotoSizeExceededException`() {
        // Arrange
        val userId = "user-123"
        val petRequest = RegisterPetRequest(
            name = "Buddy",
            species = "DOG",
            breed = null,
            age = 3,
            birthdate = null,
            weight = null,
            nickname = null
        )

        val petJson = objectMapper.writeValueAsBytes(petRequest)
        val photoBytes = "fake-large-photo".toByteArray()

        val petPart = createMockPart(petJson)
        val photoPart = createMockFilePart("large.jpg", "image/jpeg", photoBytes)

        val multipartData = LinkedMultiValueMap<String, Part>()
        multipartData.add("pet", petPart)
        multipartData.add("photo", photoPart)

        whenever(serverRequest.multipartData()).thenReturn(Mono.just(multipartData))
        whenever(registerPetUseCase.execute(any())).thenReturn(
            Mono.error(PhotoSizeExceededException(10_000_000L, 5_000_000L))
        )

        val authentication = TestingAuthenticationToken(userId, null)
        authentication.isAuthenticated = true

        // Act & Assert
        StepVerifier.create(
            petHandler.registerPet(serverRequest)
                .contextWrite(
                    org.springframework.security.core.context.ReactiveSecurityContextHolder
                        .withAuthentication(authentication)
                )
        )
            .expectNextMatches { response ->
                response.statusCode() == HttpStatus.PAYLOAD_TOO_LARGE &&
                    (response as EntityResponse<*>).let { entityResponse ->
                        val body = entityResponse.entity() as ErrorResponse
                        body.error == "PHOTO_SIZE_EXCEEDED"
                    }
            }
            .verifyComplete()
    }

    @Test
    fun `should return 500 when use case throws PhotoUploadException`() {
        // Arrange
        val userId = "user-123"
        val petRequest = RegisterPetRequest(
            name = "Buddy",
            species = "DOG",
            breed = null,
            age = 3,
            birthdate = null,
            weight = null,
            nickname = null
        )

        val petJson = objectMapper.writeValueAsBytes(petRequest)
        val photoBytes = "fake-photo".toByteArray()

        val petPart = createMockPart(petJson)
        val photoPart = createMockFilePart("photo.jpg", "image/jpeg", photoBytes)

        val multipartData = LinkedMultiValueMap<String, Part>()
        multipartData.add("pet", petPart)
        multipartData.add("photo", photoPart)

        whenever(serverRequest.multipartData()).thenReturn(Mono.just(multipartData))
        whenever(registerPetUseCase.execute(any())).thenReturn(
            Mono.error(PhotoUploadException("S3 upload failed"))
        )

        val authentication = TestingAuthenticationToken(userId, null)
        authentication.isAuthenticated = true

        // Act & Assert
        StepVerifier.create(
            petHandler.registerPet(serverRequest)
                .contextWrite(
                    org.springframework.security.core.context.ReactiveSecurityContextHolder
                        .withAuthentication(authentication)
                )
        )
            .expectNextMatches { response ->
                response.statusCode() == HttpStatus.INTERNAL_SERVER_ERROR &&
                    (response as EntityResponse<*>).let { entityResponse ->
                        val body = entityResponse.entity() as ErrorResponse
                        body.error == "PHOTO_UPLOAD_FAILED" &&
                            body.message == "S3 upload failed"
                    }
            }
            .verifyComplete()
    }

    @Test
    fun `should return 500 when use case throws unexpected exception`() {
        // Arrange
        val userId = "user-123"
        val petRequest = RegisterPetRequest(
            name = "Buddy",
            species = "DOG",
            breed = null,
            age = 3,
            birthdate = null,
            weight = null,
            nickname = null
        )

        val petJson = objectMapper.writeValueAsBytes(petRequest)
        val petPart = createMockPart(petJson)
        val multipartData = LinkedMultiValueMap<String, Part>()
        multipartData.add("pet", petPart)

        whenever(serverRequest.multipartData()).thenReturn(Mono.just(multipartData))
        whenever(registerPetUseCase.execute(any())).thenReturn(
            Mono.error(RuntimeException("Unexpected database error"))
        )

        val authentication = TestingAuthenticationToken(userId, null)
        authentication.isAuthenticated = true

        // Act & Assert
        StepVerifier.create(
            petHandler.registerPet(serverRequest)
                .contextWrite(
                    org.springframework.security.core.context.ReactiveSecurityContextHolder
                        .withAuthentication(authentication)
                )
        )
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

    // Helper functions to create mock parts
    private fun createMockPart(content: ByteArray): Part {
        val part = org.mockito.kotlin.mock<Part>()
        val dataBuffer = dataBufferFactory.wrap(content)
        whenever(part.content()).thenReturn(Flux.just(dataBuffer))
        return part
    }

    private fun createMockFilePart(fileName: String, contentType: String, content: ByteArray): FilePart {
        val filePart = org.mockito.kotlin.mock<FilePart>()
        val dataBuffer = dataBufferFactory.wrap(content)
        val headers = HttpHeaders()
        headers.contentType = MediaType.parseMediaType(contentType)

        whenever(filePart.filename()).thenReturn(fileName)
        whenever(filePart.headers()).thenReturn(headers)
        whenever(filePart.content()).thenReturn(Flux.just(dataBuffer))

        return filePart
    }
}
