package com.donperry.usecase.pet

import com.donperry.model.exception.PetNotFoundException
import com.donperry.model.exception.PhotoNotFoundException
import com.donperry.model.exception.UnauthorizedException
import com.donperry.model.exception.ValidationException
import com.donperry.model.pet.ConfirmAvatarUploadCommand
import com.donperry.model.pet.Pet
import com.donperry.model.pet.Species
import com.donperry.model.pet.gateway.PetPersistenceGateway
import com.donperry.model.pet.gateway.PhotoStorageGateway
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.eq
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import reactor.core.publisher.Mono
import reactor.test.StepVerifier
import java.time.LocalDate
import kotlin.test.assertEquals

@ExtendWith(MockitoExtension::class)
class ConfirmAvatarUploadUseCaseTest {

    @Mock
    private lateinit var petPersistenceGateway: PetPersistenceGateway

    @Mock
    private lateinit var photoStorageGateway: PhotoStorageGateway

    @InjectMocks
    private lateinit var confirmAvatarUploadUseCase: ConfirmAvatarUploadUseCase

    @Test
    fun `should confirm avatar upload when photo exists`() {
        val userId = "user-123"
        val petId = "pet-456"
        val contentType = "image/jpeg"
        val photoKey = "pets/$userId/$petId/avatar.jpg"
        val photoUrl = "https://s3.amazonaws.com/bucket/$photoKey"

        val pet = Pet(
            id = petId,
            name = "Buddy",
            species = Species.DOG,
            breed = "Golden Retriever",
            age = 3,
            owner = userId,
            registrationDate = LocalDate.now(),
            photoUrl = null
        )

        val updatedPet = pet.copy(photoUrl = photoUrl)

        whenever(photoStorageGateway.buildPhotoKey(userId, petId, contentType)).thenReturn(photoKey)
        whenever(petPersistenceGateway.findById(petId)).thenReturn(Mono.just(pet))
        whenever(photoStorageGateway.verifyPhotoExists(photoKey)).thenReturn(Mono.just(true))
        whenever(photoStorageGateway.buildPhotoUrl(photoKey)).thenReturn(photoUrl)
        whenever(petPersistenceGateway.save(any())).thenReturn(Mono.just(updatedPet))

        val command = ConfirmAvatarUploadCommand(
            userId = userId,
            petId = petId,
            contentType = contentType
        )

        StepVerifier.create(confirmAvatarUploadUseCase.execute(command))
            .expectNext(updatedPet)
            .verifyComplete()

        verify(photoStorageGateway).buildPhotoKey(userId, petId, contentType)
        verify(petPersistenceGateway).findById(petId)
        verify(photoStorageGateway).verifyPhotoExists(photoKey)
        verify(photoStorageGateway).buildPhotoUrl(photoKey)
        verify(petPersistenceGateway).save(any())
    }

    @Test
    fun `should update pet with photoUrl when confirmation succeeds`() {
        val userId = "user-123"
        val petId = "pet-456"
        val contentType = "image/png"
        val photoKey = "pets/$userId/$petId/avatar.png"
        val photoUrl = "https://cloudfront.net/$photoKey"

        val pet = Pet(
            id = petId,
            name = "Mittens",
            species = Species.CAT,
            breed = "Persian",
            age = 2,
            owner = userId,
            registrationDate = LocalDate.now(),
            photoUrl = null
        )

        val updatedPet = pet.copy(photoUrl = photoUrl)

        whenever(photoStorageGateway.buildPhotoKey(userId, petId, contentType)).thenReturn(photoKey)
        whenever(petPersistenceGateway.findById(petId)).thenReturn(Mono.just(pet))
        whenever(photoStorageGateway.verifyPhotoExists(photoKey)).thenReturn(Mono.just(true))
        whenever(photoStorageGateway.buildPhotoUrl(photoKey)).thenReturn(photoUrl)
        whenever(petPersistenceGateway.save(any())).thenReturn(Mono.just(updatedPet))

        val command = ConfirmAvatarUploadCommand(
            userId = userId,
            petId = petId,
            contentType = contentType
        )

        StepVerifier.create(confirmAvatarUploadUseCase.execute(command))
            .expectNext(updatedPet)
            .verifyComplete()

        val petCaptor = argumentCaptor<Pet>()
        verify(petPersistenceGateway).save(petCaptor.capture())
        assertEquals(photoUrl, petCaptor.firstValue.photoUrl)
    }

    @Test
    fun `should throw ValidationException when contentType is not image jpeg or png`() {
        val userId = "user-123"
        val petId = "pet-456"
        val contentType = "image/gif"

        val command = ConfirmAvatarUploadCommand(
            userId = userId,
            petId = petId,
            contentType = contentType
        )

        StepVerifier.create(confirmAvatarUploadUseCase.execute(command))
            .expectErrorMatches { throwable ->
                throwable is ValidationException &&
                throwable.message!!.contains("Invalid content type: $contentType")
            }
            .verify()

        verify(photoStorageGateway, never()).buildPhotoKey(any(), any(), any())
        verify(petPersistenceGateway, never()).findById(any())
        verify(photoStorageGateway, never()).verifyPhotoExists(any())
        verify(petPersistenceGateway, never()).save(any())
    }

    @Test
    fun `should throw ValidationException when contentType is text plain`() {
        val userId = "user-123"
        val petId = "pet-456"
        val contentType = "text/plain"

        val command = ConfirmAvatarUploadCommand(
            userId = userId,
            petId = petId,
            contentType = contentType
        )

        StepVerifier.create(confirmAvatarUploadUseCase.execute(command))
            .expectError(ValidationException::class.java)
            .verify()

        verify(photoStorageGateway, never()).buildPhotoKey(any(), any(), any())
        verify(petPersistenceGateway, never()).findById(any())
        verify(photoStorageGateway, never()).verifyPhotoExists(any())
    }

    @Test
    fun `should throw ValidationException when contentType is empty string`() {
        val userId = "user-123"
        val petId = "pet-456"
        val contentType = ""

        val command = ConfirmAvatarUploadCommand(
            userId = userId,
            petId = petId,
            contentType = contentType
        )

        StepVerifier.create(confirmAvatarUploadUseCase.execute(command))
            .expectError(ValidationException::class.java)
            .verify()

        verify(photoStorageGateway, never()).buildPhotoKey(any(), any(), any())
        verify(petPersistenceGateway, never()).findById(any())
    }

    @Test
    fun `should throw ValidationException when contentType is application pdf`() {
        val userId = "user-123"
        val petId = "pet-456"
        val contentType = "application/pdf"

        val command = ConfirmAvatarUploadCommand(
            userId = userId,
            petId = petId,
            contentType = contentType
        )

        StepVerifier.create(confirmAvatarUploadUseCase.execute(command))
            .expectError(ValidationException::class.java)
            .verify()

        verify(photoStorageGateway, never()).buildPhotoKey(any(), any(), any())
        verify(petPersistenceGateway, never()).findById(any())
    }

    @Test
    fun `should throw PetNotFoundException when pet does not exist`() {
        val userId = "user-123"
        val petId = "pet-nonexistent"
        val contentType = "image/jpeg"
        val photoKey = "pets/$userId/$petId/avatar.jpg"

        whenever(photoStorageGateway.buildPhotoKey(userId, petId, contentType)).thenReturn(photoKey)
        whenever(petPersistenceGateway.findById(petId)).thenReturn(Mono.empty())

        val command = ConfirmAvatarUploadCommand(
            userId = userId,
            petId = petId,
            contentType = contentType
        )

        StepVerifier.create(confirmAvatarUploadUseCase.execute(command))
            .expectErrorMatches { throwable ->
                throwable is PetNotFoundException &&
                throwable.message == "Pet with id $petId not found"
            }
            .verify()

        verify(photoStorageGateway).buildPhotoKey(userId, petId, contentType)
        verify(petPersistenceGateway).findById(petId)
        verify(photoStorageGateway, never()).verifyPhotoExists(any())
        verify(petPersistenceGateway, never()).save(any())
    }

    @Test
    fun `should throw UnauthorizedException when user is not pet owner`() {
        val userId = "user-123"
        val actualOwnerId = "user-999"
        val petId = "pet-456"
        val contentType = "image/jpeg"
        val photoKey = "pets/$userId/$petId/avatar.jpg"

        val pet = Pet(
            id = petId,
            name = "Buddy",
            species = Species.DOG,
            breed = "Golden Retriever",
            age = 3,
            owner = actualOwnerId,
            registrationDate = LocalDate.now()
        )

        whenever(photoStorageGateway.buildPhotoKey(userId, petId, contentType)).thenReturn(photoKey)
        whenever(petPersistenceGateway.findById(petId)).thenReturn(Mono.just(pet))

        val command = ConfirmAvatarUploadCommand(
            userId = userId,
            petId = petId,
            contentType = contentType
        )

        StepVerifier.create(confirmAvatarUploadUseCase.execute(command))
            .expectErrorMatches { throwable ->
                throwable is UnauthorizedException &&
                throwable.message == "User $userId is not the owner of pet $petId"
            }
            .verify()

        verify(photoStorageGateway).buildPhotoKey(userId, petId, contentType)
        verify(petPersistenceGateway).findById(petId)
        verify(photoStorageGateway, never()).verifyPhotoExists(any())
        verify(petPersistenceGateway, never()).save(any())
    }

    @Test
    fun `should throw UnauthorizedException when userId does not match owner`() {
        val userId = "user-123"
        val petId = "pet-456"
        val contentType = "image/jpeg"
        val photoKey = "pets/$userId/$petId/avatar.jpg"

        val pet = Pet(
            id = petId,
            name = "Mittens",
            species = Species.CAT,
            breed = "Persian",
            age = 2,
            owner = "different-user",
            registrationDate = LocalDate.now()
        )

        whenever(photoStorageGateway.buildPhotoKey(userId, petId, contentType)).thenReturn(photoKey)
        whenever(petPersistenceGateway.findById(petId)).thenReturn(Mono.just(pet))

        val command = ConfirmAvatarUploadCommand(
            userId = userId,
            petId = petId,
            contentType = contentType
        )

        StepVerifier.create(confirmAvatarUploadUseCase.execute(command))
            .expectError(UnauthorizedException::class.java)
            .verify()

        verify(photoStorageGateway).buildPhotoKey(userId, petId, contentType)
        verify(petPersistenceGateway).findById(petId)
        verify(photoStorageGateway, never()).verifyPhotoExists(any())
    }

    @Test
    fun `should throw PhotoNotFoundException when photo not in S3`() {
        val userId = "user-123"
        val petId = "pet-456"
        val contentType = "image/jpeg"
        val photoKey = "pets/$userId/$petId/avatar.jpg"

        val pet = Pet(
            id = petId,
            name = "Buddy",
            species = Species.DOG,
            breed = "Golden Retriever",
            age = 3,
            owner = userId,
            registrationDate = LocalDate.now()
        )

        whenever(photoStorageGateway.buildPhotoKey(userId, petId, contentType)).thenReturn(photoKey)
        whenever(petPersistenceGateway.findById(petId)).thenReturn(Mono.just(pet))
        whenever(photoStorageGateway.verifyPhotoExists(photoKey)).thenReturn(Mono.just(false))

        val command = ConfirmAvatarUploadCommand(
            userId = userId,
            petId = petId,
            contentType = contentType
        )

        StepVerifier.create(confirmAvatarUploadUseCase.execute(command))
            .expectErrorMatches { throwable ->
                throwable is PhotoNotFoundException &&
                throwable.message == "Photo not found in storage: $photoKey"
            }
            .verify()

        verify(photoStorageGateway).buildPhotoKey(userId, petId, contentType)
        verify(petPersistenceGateway).findById(petId)
        verify(photoStorageGateway).verifyPhotoExists(photoKey)
        verify(photoStorageGateway, never()).buildPhotoUrl(any())
        verify(petPersistenceGateway, never()).save(any())
    }

    @Test
    fun `should throw PhotoNotFoundException when verifyPhotoExists returns false`() {
        val userId = "user-123"
        val petId = "pet-456"
        val contentType = "image/png"
        val photoKey = "pets/$userId/$petId/avatar.png"

        val pet = Pet(
            id = petId,
            name = "Buddy",
            species = Species.DOG,
            breed = "Golden Retriever",
            age = 3,
            owner = userId,
            registrationDate = LocalDate.now()
        )

        whenever(photoStorageGateway.buildPhotoKey(userId, petId, contentType)).thenReturn(photoKey)
        whenever(petPersistenceGateway.findById(petId)).thenReturn(Mono.just(pet))
        whenever(photoStorageGateway.verifyPhotoExists(photoKey)).thenReturn(Mono.just(false))

        val command = ConfirmAvatarUploadCommand(
            userId = userId,
            petId = petId,
            contentType = contentType
        )

        StepVerifier.create(confirmAvatarUploadUseCase.execute(command))
            .expectError(PhotoNotFoundException::class.java)
            .verify()

        verify(photoStorageGateway).buildPhotoKey(userId, petId, contentType)
        verify(photoStorageGateway).verifyPhotoExists(photoKey)
        verify(petPersistenceGateway, never()).save(any())
    }

    @Test
    fun `should propagate error when findById fails`() {
        val userId = "user-123"
        val petId = "pet-456"
        val contentType = "image/jpeg"
        val photoKey = "pets/$userId/$petId/avatar.jpg"
        val dbError = RuntimeException("Database connection failed")

        whenever(photoStorageGateway.buildPhotoKey(userId, petId, contentType)).thenReturn(photoKey)
        whenever(petPersistenceGateway.findById(petId)).thenReturn(Mono.error(dbError))

        val command = ConfirmAvatarUploadCommand(
            userId = userId,
            petId = petId,
            contentType = contentType
        )

        StepVerifier.create(confirmAvatarUploadUseCase.execute(command))
            .expectErrorMatches { throwable ->
                throwable is RuntimeException && throwable.message == "Database connection failed"
            }
            .verify()

        verify(photoStorageGateway).buildPhotoKey(userId, petId, contentType)
        verify(petPersistenceGateway).findById(petId)
        verify(photoStorageGateway, never()).verifyPhotoExists(any())
        verify(petPersistenceGateway, never()).save(any())
    }

    @Test
    fun `should propagate error when verifyPhotoExists fails`() {
        val userId = "user-123"
        val petId = "pet-456"
        val contentType = "image/jpeg"
        val photoKey = "pets/$userId/$petId/avatar.jpg"

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

        whenever(photoStorageGateway.buildPhotoKey(userId, petId, contentType)).thenReturn(photoKey)
        whenever(petPersistenceGateway.findById(petId)).thenReturn(Mono.just(pet))
        whenever(photoStorageGateway.verifyPhotoExists(photoKey)).thenReturn(Mono.error(s3Error))

        val command = ConfirmAvatarUploadCommand(
            userId = userId,
            petId = petId,
            contentType = contentType
        )

        StepVerifier.create(confirmAvatarUploadUseCase.execute(command))
            .expectErrorMatches { throwable ->
                throwable is RuntimeException && throwable.message == "S3 service unavailable"
            }
            .verify()

        verify(photoStorageGateway).buildPhotoKey(userId, petId, contentType)
        verify(petPersistenceGateway).findById(petId)
        verify(photoStorageGateway).verifyPhotoExists(photoKey)
        verify(petPersistenceGateway, never()).save(any())
    }

    @Test
    fun `should propagate error when save fails`() {
        val userId = "user-123"
        val petId = "pet-456"
        val contentType = "image/jpeg"
        val photoKey = "pets/$userId/$petId/avatar.jpg"
        val photoUrl = "https://s3.amazonaws.com/bucket/$photoKey"

        val pet = Pet(
            id = petId,
            name = "Buddy",
            species = Species.DOG,
            breed = "Golden Retriever",
            age = 3,
            owner = userId,
            registrationDate = LocalDate.now()
        )

        val saveError = RuntimeException("Database constraint violation")

        whenever(photoStorageGateway.buildPhotoKey(userId, petId, contentType)).thenReturn(photoKey)
        whenever(petPersistenceGateway.findById(petId)).thenReturn(Mono.just(pet))
        whenever(photoStorageGateway.verifyPhotoExists(photoKey)).thenReturn(Mono.just(true))
        whenever(photoStorageGateway.buildPhotoUrl(photoKey)).thenReturn(photoUrl)
        whenever(petPersistenceGateway.save(any())).thenReturn(Mono.error(saveError))

        val command = ConfirmAvatarUploadCommand(
            userId = userId,
            petId = petId,
            contentType = contentType
        )

        StepVerifier.create(confirmAvatarUploadUseCase.execute(command))
            .expectErrorMatches { throwable ->
                throwable is RuntimeException && throwable.message == "Database constraint violation"
            }
            .verify()

        verify(photoStorageGateway).buildPhotoKey(userId, petId, contentType)
        verify(petPersistenceGateway).findById(petId)
        verify(photoStorageGateway).verifyPhotoExists(photoKey)
        verify(photoStorageGateway).buildPhotoUrl(photoKey)
        verify(petPersistenceGateway).save(any())
    }

    @Test
    fun `should call buildPhotoUrl with correct photoKey`() {
        val userId = "user-123"
        val petId = "pet-456"
        val contentType = "image/jpeg"
        val photoKey = "pets/$userId/$petId/avatar.jpg"
        val photoUrl = "https://cdn.example.com/$photoKey"

        val pet = Pet(
            id = petId,
            name = "Buddy",
            species = Species.DOG,
            breed = "Golden Retriever",
            age = 3,
            owner = userId,
            registrationDate = LocalDate.now()
        )

        val updatedPet = pet.copy(photoUrl = photoUrl)

        whenever(photoStorageGateway.buildPhotoKey(userId, petId, contentType)).thenReturn(photoKey)
        whenever(petPersistenceGateway.findById(petId)).thenReturn(Mono.just(pet))
        whenever(photoStorageGateway.verifyPhotoExists(photoKey)).thenReturn(Mono.just(true))
        whenever(photoStorageGateway.buildPhotoUrl(photoKey)).thenReturn(photoUrl)
        whenever(petPersistenceGateway.save(any())).thenReturn(Mono.just(updatedPet))

        val command = ConfirmAvatarUploadCommand(
            userId = userId,
            petId = petId,
            contentType = contentType
        )

        StepVerifier.create(confirmAvatarUploadUseCase.execute(command))
            .expectNext(updatedPet)
            .verifyComplete()

        verify(photoStorageGateway).buildPhotoKey(userId, petId, contentType)
        verify(photoStorageGateway).buildPhotoUrl(photoKey)
    }

    @Test
    fun `should replace existing photoUrl when confirming new upload`() {
        val userId = "user-123"
        val petId = "pet-456"
        val contentType = "image/jpeg"
        val photoKey = "pets/$userId/$petId/avatar.jpg"
        val newPhotoUrl = "https://s3.amazonaws.com/bucket/$photoKey"

        val pet = Pet(
            id = petId,
            name = "Buddy",
            species = Species.DOG,
            breed = "Golden Retriever",
            age = 3,
            owner = userId,
            registrationDate = LocalDate.now(),
            photoUrl = "https://s3.amazonaws.com/bucket/old-photo.jpg"
        )

        val updatedPet = pet.copy(photoUrl = newPhotoUrl)

        whenever(photoStorageGateway.buildPhotoKey(userId, petId, contentType)).thenReturn(photoKey)
        whenever(petPersistenceGateway.findById(petId)).thenReturn(Mono.just(pet))
        whenever(photoStorageGateway.verifyPhotoExists(photoKey)).thenReturn(Mono.just(true))
        whenever(photoStorageGateway.buildPhotoUrl(photoKey)).thenReturn(newPhotoUrl)
        whenever(petPersistenceGateway.save(any())).thenReturn(Mono.just(updatedPet))

        val command = ConfirmAvatarUploadCommand(
            userId = userId,
            petId = petId,
            contentType = contentType
        )

        StepVerifier.create(confirmAvatarUploadUseCase.execute(command))
            .expectNext(updatedPet)
            .verifyComplete()

        val petCaptor = argumentCaptor<Pet>()
        verify(petPersistenceGateway).save(petCaptor.capture())
        assertEquals(newPhotoUrl, petCaptor.firstValue.photoUrl)
    }
}
