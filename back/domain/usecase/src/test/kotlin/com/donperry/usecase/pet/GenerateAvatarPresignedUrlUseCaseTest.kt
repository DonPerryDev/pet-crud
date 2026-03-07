package com.donperry.usecase.pet

import com.donperry.model.exception.PetNotFoundException
import com.donperry.model.exception.UnauthorizedException
import com.donperry.model.exception.ValidationException
import com.donperry.model.pet.GeneratePresignedUrlCommand
import com.donperry.model.pet.Pet
import com.donperry.model.pet.PresignedUploadUrl
import com.donperry.model.pet.Species
import com.donperry.model.pet.gateway.PetPersistenceGateway
import com.donperry.model.pet.gateway.PhotoStorageGateway
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import reactor.core.publisher.Mono
import reactor.test.StepVerifier
import java.time.Instant
import java.time.LocalDate

@ExtendWith(MockitoExtension::class)
class GenerateAvatarPresignedUrlUseCaseTest {

    @Mock
    private lateinit var petPersistenceGateway: PetPersistenceGateway

    @Mock
    private lateinit var photoStorageGateway: PhotoStorageGateway

    @InjectMocks
    private lateinit var generateAvatarPresignedUrlUseCase: GenerateAvatarPresignedUrlUseCase

    @Test
    fun `should generate presigned URL when contentType is jpeg`() {
        val userId = "user-123"
        val petId = "pet-456"
        val contentType = "image/jpeg"

        val pet = Pet(
            id = petId,
            name = "Buddy",
            species = Species.DOG,
            breed = "Golden Retriever",
            age = 3,
            owner = userId,
            registrationDate = LocalDate.now()
        )

        val presignedUrl = PresignedUploadUrl(
            uploadUrl = "https://s3.amazonaws.com/bucket/upload?signature=abc123",
            key = "pets/$userId/$petId/photo.jpg",
            expiresAt = Instant.now().plusSeconds(900)
        )

        whenever(petPersistenceGateway.findById(petId)).thenReturn(Mono.just(pet))
        whenever(photoStorageGateway.generatePresignedUrl(eq(userId), eq(petId), eq(contentType), eq(15)))
            .thenReturn(Mono.just(presignedUrl))

        val command = GeneratePresignedUrlCommand(
            userId = userId,
            petId = petId,
            contentType = contentType
        )

        StepVerifier.create(generateAvatarPresignedUrlUseCase.execute(command))
            .expectNext(presignedUrl)
            .verifyComplete()

        verify(petPersistenceGateway).findById(petId)
        verify(photoStorageGateway).generatePresignedUrl(userId, petId, contentType, 15)
    }

    @Test
    fun `should generate presigned URL when contentType is png`() {
        val userId = "user-123"
        val petId = "pet-456"
        val contentType = "image/png"

        val pet = Pet(
            id = petId,
            name = "Mittens",
            species = Species.CAT,
            breed = "Persian",
            age = 2,
            owner = userId,
            registrationDate = LocalDate.now()
        )

        val presignedUrl = PresignedUploadUrl(
            uploadUrl = "https://s3.amazonaws.com/bucket/upload?signature=xyz789",
            key = "pets/$userId/$petId/photo.png",
            expiresAt = Instant.now().plusSeconds(900)
        )

        whenever(petPersistenceGateway.findById(petId)).thenReturn(Mono.just(pet))
        whenever(photoStorageGateway.generatePresignedUrl(eq(userId), eq(petId), eq(contentType), eq(15)))
            .thenReturn(Mono.just(presignedUrl))

        val command = GeneratePresignedUrlCommand(
            userId = userId,
            petId = petId,
            contentType = contentType
        )

        StepVerifier.create(generateAvatarPresignedUrlUseCase.execute(command))
            .expectNext(presignedUrl)
            .verifyComplete()

        verify(petPersistenceGateway).findById(petId)
        verify(photoStorageGateway).generatePresignedUrl(userId, petId, contentType, 15)
    }

    @Test
    fun `should throw ValidationException when contentType is invalid`() {
        val userId = "user-123"
        val petId = "pet-456"
        val contentType = "image/gif"

        val command = GeneratePresignedUrlCommand(
            userId = userId,
            petId = petId,
            contentType = contentType
        )

        StepVerifier.create(generateAvatarPresignedUrlUseCase.execute(command))
            .expectErrorMatches { throwable ->
                throwable is ValidationException &&
                throwable.message!!.contains("Invalid content type: $contentType") &&
                throwable.message!!.contains("image/jpeg") &&
                throwable.message!!.contains("image/png")
            }
            .verify()

        verify(petPersistenceGateway, never()).findById(any())
        verify(photoStorageGateway, never()).generatePresignedUrl(any(), any(), any(), any())
    }

    @Test
    fun `should throw ValidationException when contentType is text`() {
        val command = GeneratePresignedUrlCommand(
            userId = "user-123",
            petId = "pet-456",
            contentType = "text/plain"
        )

        StepVerifier.create(generateAvatarPresignedUrlUseCase.execute(command))
            .expectError(ValidationException::class.java)
            .verify()

        verify(petPersistenceGateway, never()).findById(any())
        verify(photoStorageGateway, never()).generatePresignedUrl(any(), any(), any(), any())
    }

    @Test
    fun `should throw ValidationException when contentType is application type`() {
        val command = GeneratePresignedUrlCommand(
            userId = "user-123",
            petId = "pet-456",
            contentType = "application/pdf"
        )

        StepVerifier.create(generateAvatarPresignedUrlUseCase.execute(command))
            .expectError(ValidationException::class.java)
            .verify()

        verify(petPersistenceGateway, never()).findById(any())
    }

    @Test
    fun `should throw PetNotFoundException when pet does not exist`() {
        val userId = "user-123"
        val petId = "pet-nonexistent"
        val contentType = "image/jpeg"

        whenever(petPersistenceGateway.findById(petId)).thenReturn(Mono.empty())

        val command = GeneratePresignedUrlCommand(
            userId = userId,
            petId = petId,
            contentType = contentType
        )

        StepVerifier.create(generateAvatarPresignedUrlUseCase.execute(command))
            .expectErrorMatches { throwable ->
                throwable is PetNotFoundException &&
                throwable.message == "Pet with id $petId not found"
            }
            .verify()

        verify(petPersistenceGateway).findById(petId)
        verify(photoStorageGateway, never()).generatePresignedUrl(any(), any(), any(), any())
    }

    @Test
    fun `should throw UnauthorizedException when user is not pet owner`() {
        val userId = "user-123"
        val actualOwnerId = "user-999"
        val petId = "pet-456"
        val contentType = "image/jpeg"

        val pet = Pet(
            id = petId,
            name = "Buddy",
            species = Species.DOG,
            breed = "Golden Retriever",
            age = 3,
            owner = actualOwnerId,
            registrationDate = LocalDate.now()
        )

        whenever(petPersistenceGateway.findById(petId)).thenReturn(Mono.just(pet))

        val command = GeneratePresignedUrlCommand(
            userId = userId,
            petId = petId,
            contentType = contentType
        )

        StepVerifier.create(generateAvatarPresignedUrlUseCase.execute(command))
            .expectErrorMatches { throwable ->
                throwable is UnauthorizedException &&
                throwable.message == "User $userId is not the owner of pet $petId"
            }
            .verify()

        verify(petPersistenceGateway).findById(petId)
        verify(photoStorageGateway, never()).generatePresignedUrl(any(), any(), any(), any())
    }

    @Test
    fun `should throw UnauthorizedException when userId does not match owner`() {
        val userId = "user-123"
        val petId = "pet-456"
        val contentType = "image/png"

        val pet = Pet(
            id = petId,
            name = "Mittens",
            species = Species.CAT,
            breed = "Persian",
            age = 2,
            owner = "different-user",
            registrationDate = LocalDate.now()
        )

        whenever(petPersistenceGateway.findById(petId)).thenReturn(Mono.just(pet))

        val command = GeneratePresignedUrlCommand(
            userId = userId,
            petId = petId,
            contentType = contentType
        )

        StepVerifier.create(generateAvatarPresignedUrlUseCase.execute(command))
            .expectError(UnauthorizedException::class.java)
            .verify()

        verify(petPersistenceGateway).findById(petId)
        verify(photoStorageGateway, never()).generatePresignedUrl(any(), any(), any(), any())
    }

    @Test
    fun `should propagate error when findById fails`() {
        val userId = "user-123"
        val petId = "pet-456"
        val contentType = "image/jpeg"
        val dbError = RuntimeException("Database connection failed")

        whenever(petPersistenceGateway.findById(petId)).thenReturn(Mono.error(dbError))

        val command = GeneratePresignedUrlCommand(
            userId = userId,
            petId = petId,
            contentType = contentType
        )

        StepVerifier.create(generateAvatarPresignedUrlUseCase.execute(command))
            .expectErrorMatches { throwable ->
                throwable is RuntimeException && throwable.message == "Database connection failed"
            }
            .verify()

        verify(petPersistenceGateway).findById(petId)
        verify(photoStorageGateway, never()).generatePresignedUrl(any(), any(), any(), any())
    }

    @Test
    fun `should propagate error when generatePresignedUrl fails`() {
        val userId = "user-123"
        val petId = "pet-456"
        val contentType = "image/jpeg"

        val pet = Pet(
            id = petId,
            name = "Buddy",
            species = Species.DOG,
            breed = "Golden Retriever",
            age = 3,
            owner = userId,
            registrationDate = LocalDate.now()
        )

        val s3Error = RuntimeException("S3 service unavailable")

        whenever(petPersistenceGateway.findById(petId)).thenReturn(Mono.just(pet))
        whenever(photoStorageGateway.generatePresignedUrl(eq(userId), eq(petId), eq(contentType), eq(15)))
            .thenReturn(Mono.error(s3Error))

        val command = GeneratePresignedUrlCommand(
            userId = userId,
            petId = petId,
            contentType = contentType
        )

        StepVerifier.create(generateAvatarPresignedUrlUseCase.execute(command))
            .expectErrorMatches { throwable ->
                throwable is RuntimeException && throwable.message == "S3 service unavailable"
            }
            .verify()

        verify(petPersistenceGateway).findById(petId)
        verify(photoStorageGateway).generatePresignedUrl(userId, petId, contentType, 15)
    }

    @Test
    fun `should use 15 minute expiration for presigned URL`() {
        val userId = "user-123"
        val petId = "pet-456"
        val contentType = "image/jpeg"

        val pet = Pet(
            id = petId,
            name = "Buddy",
            species = Species.DOG,
            breed = "Golden Retriever",
            age = 3,
            owner = userId,
            registrationDate = LocalDate.now()
        )

        val presignedUrl = PresignedUploadUrl(
            uploadUrl = "https://s3.amazonaws.com/bucket/upload",
            key = "pets/$userId/$petId/photo.jpg",
            expiresAt = Instant.now().plusSeconds(900)
        )

        whenever(petPersistenceGateway.findById(petId)).thenReturn(Mono.just(pet))
        whenever(photoStorageGateway.generatePresignedUrl(eq(userId), eq(petId), eq(contentType), eq(15)))
            .thenReturn(Mono.just(presignedUrl))

        val command = GeneratePresignedUrlCommand(
            userId = userId,
            petId = petId,
            contentType = contentType
        )

        StepVerifier.create(generateAvatarPresignedUrlUseCase.execute(command))
            .expectNext(presignedUrl)
            .verifyComplete()

        verify(photoStorageGateway).generatePresignedUrl(userId, petId, contentType, 15)
    }
}
