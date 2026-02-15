package com.donperry.usecase.pet

import com.donperry.model.exception.PetLimitExceededException
import com.donperry.model.exception.PhotoSizeExceededException
import com.donperry.model.exception.PhotoUploadException
import com.donperry.model.exception.ValidationException
import com.donperry.model.pet.Pet
import com.donperry.model.pet.PhotoUploadData
import com.donperry.model.pet.RegisterPetCommand
import com.donperry.model.pet.Species
import com.donperry.model.pet.gateway.PetPersistenceGateway
import com.donperry.model.pet.gateway.PhotoStorageGateway
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.eq
import org.mockito.kotlin.never
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import reactor.core.publisher.Mono
import reactor.test.StepVerifier
import java.math.BigDecimal
import java.time.LocalDate
import kotlin.test.assertEquals
import kotlin.test.assertNull

@ExtendWith(MockitoExtension::class)
class RegisterPetUseCaseTest {

    @Mock
    private lateinit var petPersistenceGateway: PetPersistenceGateway

    @Mock
    private lateinit var photoStorageGateway: PhotoStorageGateway

    private lateinit var registerPetUseCase: RegisterPetUseCase

    @BeforeEach
    fun setUp() {
        registerPetUseCase = RegisterPetUseCase(petPersistenceGateway, photoStorageGateway)
    }

    // Happy path tests
    @Test
    fun `should register pet without photo when all required fields valid`() {
        val userId = "user-123"
        val savedPet = Pet(
            id = "pet-123",
            name = "Buddy",
            species = Species.DOG,
            breed = "Golden Retriever",
            age = 3,
            owner = userId,
            registrationDate = LocalDate.now()
        )

        `when`(petPersistenceGateway.countByOwner(userId)).thenReturn(Mono.just(0L))
        `when`(petPersistenceGateway.save(any())).thenReturn(Mono.just(savedPet))

        val command = RegisterPetCommand(
            userId = userId,
            name = "Buddy",
            species = Species.DOG,
            breed = "Golden Retriever",
            age = 3,
            birthdate = null,
            weight = null,
            nickname = null,
            photo = null
        )

        StepVerifier.create(registerPetUseCase.execute(command))
            .expectNext(savedPet)
            .verifyComplete()

        verify(petPersistenceGateway).countByOwner(userId)
        verify(petPersistenceGateway).save(any())
        verify(photoStorageGateway, never()).uploadPhoto(any(), any(), any())
    }

    @Test
    fun `should register pet with all optional fields populated`() {
        val userId = "user-123"
        val birthdate = LocalDate.of(2020, 5, 15)
        val weight = BigDecimal("30.0")

        val savedPet = Pet(
            id = "pet-123",
            name = "Buddy",
            species = Species.DOG,
            breed = "Golden Retriever",
            age = 3,
            birthdate = birthdate,
            weight = weight,
            nickname = "Bud",
            owner = userId,
            registrationDate = LocalDate.now()
        )

        `when`(petPersistenceGateway.countByOwner(userId)).thenReturn(Mono.just(0L))
        `when`(petPersistenceGateway.save(any())).thenReturn(Mono.just(savedPet))

        val command = RegisterPetCommand(
            userId = userId,
            name = "Buddy",
            species = Species.DOG,
            breed = "Golden Retriever",
            age = 3,
            birthdate = birthdate,
            weight = weight,
            nickname = "Bud",
            photo = null
        )

        StepVerifier.create(registerPetUseCase.execute(command))
            .expectNext(savedPet)
            .verifyComplete()

        val petCaptor = argumentCaptor<Pet>()
        verify(petPersistenceGateway).save(petCaptor.capture())

        val capturedPet = petCaptor.firstValue
        assertEquals("Buddy", capturedPet.name)
        assertEquals(Species.DOG, capturedPet.species)
        assertEquals("Golden Retriever", capturedPet.breed)
        assertEquals(3, capturedPet.age)
        assertEquals(birthdate, capturedPet.birthdate)
        assertEquals(weight, capturedPet.weight)
        assertEquals("Bud", capturedPet.nickname)
        assertEquals(userId, capturedPet.owner)
        assertNull(capturedPet.id)
        assertEquals(LocalDate.now(), capturedPet.registrationDate)
    }

    @Test
    fun `should register pet with photo and update photoUrl`() {
        val userId = "user-123"
        val photoBytes = ByteArray(1024) { it.toByte() }
        val photo = PhotoUploadData(
            fileName = "buddy.jpg",
            contentType = "image/jpeg",
            fileSize = photoBytes.size.toLong(),
            fileBytes = photoBytes
        )
        val photoUrl = "https://s3.amazonaws.com/pets/user-123/pet-123/buddy.jpg"

        val savedPetWithoutPhoto = Pet(
            id = "pet-123",
            name = "Buddy",
            species = Species.DOG,
            breed = "Golden Retriever",
            age = 3,
            owner = userId,
            registrationDate = LocalDate.now()
        )
        val savedPetWithPhoto = savedPetWithoutPhoto.copy(photoUrl = photoUrl)

        `when`(petPersistenceGateway.countByOwner(userId)).thenReturn(Mono.just(0L))
        `when`(petPersistenceGateway.save(any())).thenReturn(
            Mono.just(savedPetWithoutPhoto),
            Mono.just(savedPetWithPhoto)
        )
        `when`(photoStorageGateway.uploadPhoto(eq(userId), eq("pet-123"), any()))
            .thenReturn(Mono.just(photoUrl))

        val command = RegisterPetCommand(
            userId = userId,
            name = "Buddy",
            species = Species.DOG,
            breed = "Golden Retriever",
            age = 3,
            birthdate = null,
            weight = null,
            nickname = null,
            photo = photo
        )

        StepVerifier.create(registerPetUseCase.execute(command))
            .expectNext(savedPetWithPhoto)
            .verifyComplete()

        verify(petPersistenceGateway, times(2)).save(any())
        verify(photoStorageGateway).uploadPhoto(eq(userId), eq("pet-123"), any())
    }

    @Test
    fun `should handle null optional fields correctly`() {
        val userId = "user-456"

        val savedPet = Pet(
            id = "pet-456",
            name = "Mittens",
            species = Species.CAT,
            breed = null,
            age = 2,
            owner = userId,
            registrationDate = LocalDate.now()
        )

        `when`(petPersistenceGateway.countByOwner(userId)).thenReturn(Mono.just(1L))
        `when`(petPersistenceGateway.save(any())).thenReturn(Mono.just(savedPet))

        val command = RegisterPetCommand(
            userId = userId,
            name = "Mittens",
            species = Species.CAT,
            breed = null,
            age = 2,
            birthdate = null,
            weight = null,
            nickname = null,
            photo = null
        )

        StepVerifier.create(registerPetUseCase.execute(command))
            .expectNext(savedPet)
            .verifyComplete()

        val petCaptor = argumentCaptor<Pet>()
        verify(petPersistenceGateway).save(petCaptor.capture())

        val capturedPet = petCaptor.firstValue
        assertNull(capturedPet.breed)
        assertNull(capturedPet.birthdate)
        assertNull(capturedPet.weight)
        assertNull(capturedPet.nickname)
    }

    @Test
    fun `should accept zero age as valid`() {
        val userId = "user-000"

        val savedPet = Pet(
            id = "pet-000",
            name = "Puppy",
            species = Species.DOG,
            breed = "Mixed",
            age = 0,
            owner = userId,
            registrationDate = LocalDate.now()
        )

        `when`(petPersistenceGateway.countByOwner(userId)).thenReturn(Mono.just(0L))
        `when`(petPersistenceGateway.save(any())).thenReturn(Mono.just(savedPet))

        val command = RegisterPetCommand(
            userId = userId,
            name = "Puppy",
            species = Species.DOG,
            breed = "Mixed",
            age = 0,
            birthdate = null,
            weight = null,
            nickname = null,
            photo = null
        )

        StepVerifier.create(registerPetUseCase.execute(command))
            .expectNext(savedPet)
            .verifyComplete()

        val petCaptor = argumentCaptor<Pet>()
        verify(petPersistenceGateway).save(petCaptor.capture())
        assertEquals(0, petCaptor.firstValue.age)
    }

    // Validation error tests
    @Test
    fun `should throw ValidationException when userId is blank`() {
        val command = RegisterPetCommand(
            userId = "",
            name = "Buddy",
            species = Species.DOG,
            breed = "Golden Retriever",
            age = 3,
            birthdate = null,
            weight = null,
            nickname = null,
            photo = null
        )

        StepVerifier.create(registerPetUseCase.execute(command))
            .expectErrorMatches { throwable ->
                throwable is ValidationException && throwable.message == "User ID cannot be blank"
            }
            .verify()

        verify(petPersistenceGateway, never()).save(any())
    }

    @Test
    fun `should throw ValidationException when name is blank`() {
        val command = RegisterPetCommand(
            userId = "user-123",
            name = "",
            species = Species.DOG,
            breed = "Golden Retriever",
            age = 3,
            birthdate = null,
            weight = null,
            nickname = null,
            photo = null
        )

        StepVerifier.create(registerPetUseCase.execute(command))
            .expectErrorMatches { throwable ->
                throwable is ValidationException && throwable.message == "Pet name cannot be blank"
            }
            .verify()

        verify(petPersistenceGateway, never()).save(any())
    }

    @Test
    fun `should throw ValidationException when age is negative`() {
        val command = RegisterPetCommand(
            userId = "user-123",
            name = "Buddy",
            species = Species.DOG,
            breed = "Golden Retriever",
            age = -1,
            birthdate = null,
            weight = null,
            nickname = null,
            photo = null
        )

        StepVerifier.create(registerPetUseCase.execute(command))
            .expectErrorMatches { throwable ->
                throwable is ValidationException && throwable.message == "Pet age must be zero or greater"
            }
            .verify()

        verify(petPersistenceGateway, never()).save(any())
    }

    @Test
    fun `should throw ValidationException when birthdate is in future`() {
        val futureBirthdate = LocalDate.now().plusDays(1)

        val command = RegisterPetCommand(
            userId = "user-123",
            name = "Buddy",
            species = Species.DOG,
            breed = "Golden Retriever",
            age = 3,
            birthdate = futureBirthdate,
            weight = null,
            nickname = null,
            photo = null
        )

        StepVerifier.create(registerPetUseCase.execute(command))
            .expectErrorMatches { throwable ->
                throwable is ValidationException && throwable.message == "Pet birthdate cannot be in the future"
            }
            .verify()

        verify(petPersistenceGateway, never()).save(any())
    }

    @Test
    fun `should throw ValidationException when weight is zero`() {
        val command = RegisterPetCommand(
            userId = "user-123",
            name = "Buddy",
            species = Species.DOG,
            breed = "Golden Retriever",
            age = 3,
            birthdate = null,
            weight = BigDecimal.ZERO,
            nickname = null,
            photo = null
        )

        StepVerifier.create(registerPetUseCase.execute(command))
            .expectErrorMatches { throwable ->
                throwable is ValidationException && throwable.message == "Pet weight must be greater than zero"
            }
            .verify()

        verify(petPersistenceGateway, never()).save(any())
    }

    @Test
    fun `should throw ValidationException when weight is negative`() {
        val command = RegisterPetCommand(
            userId = "user-123",
            name = "Buddy",
            species = Species.DOG,
            breed = "Golden Retriever",
            age = 3,
            birthdate = null,
            weight = BigDecimal("-5.0"),
            nickname = null,
            photo = null
        )

        StepVerifier.create(registerPetUseCase.execute(command))
            .expectErrorMatches { throwable ->
                throwable is ValidationException && throwable.message == "Pet weight must be greater than zero"
            }
            .verify()

        verify(petPersistenceGateway, never()).save(any())
    }

    // Pet limit tests
    @Test
    fun `should throw PetLimitExceededException when user has 10 pets`() {
        val userId = "user-123"

        `when`(petPersistenceGateway.countByOwner(userId)).thenReturn(Mono.just(10L))

        val command = RegisterPetCommand(
            userId = userId,
            name = "Buddy",
            species = Species.DOG,
            breed = "Golden Retriever",
            age = 3,
            birthdate = null,
            weight = null,
            nickname = null,
            photo = null
        )

        StepVerifier.create(registerPetUseCase.execute(command))
            .expectErrorMatches { throwable ->
                throwable is PetLimitExceededException &&
                throwable.message == "User $userId has reached the maximum limit of 10 pets"
            }
            .verify()

        verify(petPersistenceGateway).countByOwner(userId)
        verify(petPersistenceGateway, never()).save(any())
    }

    @Test
    fun `should allow registration when user has 9 pets`() {
        val userId = "user-123"
        val savedPet = Pet(
            id = "pet-123",
            name = "Buddy",
            species = Species.DOG,
            breed = "Golden Retriever",
            age = 3,
            owner = userId,
            registrationDate = LocalDate.now()
        )

        `when`(petPersistenceGateway.countByOwner(userId)).thenReturn(Mono.just(9L))
        `when`(petPersistenceGateway.save(any())).thenReturn(Mono.just(savedPet))

        val command = RegisterPetCommand(
            userId = userId,
            name = "Buddy",
            species = Species.DOG,
            breed = "Golden Retriever",
            age = 3,
            birthdate = null,
            weight = null,
            nickname = null,
            photo = null
        )

        StepVerifier.create(registerPetUseCase.execute(command))
            .expectNext(savedPet)
            .verifyComplete()

        verify(petPersistenceGateway).countByOwner(userId)
        verify(petPersistenceGateway).save(any())
    }

    // Photo validation tests
    @Test
    fun `should throw PhotoSizeExceededException when photo exceeds 5MB`() {
        val photoSize = 6L * 1024 * 1024

        val command = RegisterPetCommand(
            userId = "user-123",
            name = "Buddy",
            species = Species.DOG,
            breed = "Golden Retriever",
            age = 3,
            birthdate = null,
            weight = null,
            nickname = null,
            photo = PhotoUploadData(
                fileName = "buddy.jpg",
                contentType = "image/jpeg",
                fileSize = photoSize,
                fileBytes = ByteArray(1024)
            )
        )

        StepVerifier.create(registerPetUseCase.execute(command))
            .expectErrorMatches { throwable ->
                throwable is PhotoSizeExceededException &&
                throwable.message!!.contains("exceeds maximum allowed size")
            }
            .verify()

        verify(petPersistenceGateway, never()).save(any())
    }

    @Test
    fun `should accept photo of exactly 5MB`() {
        val userId = "user-123"
        val photoSize = 5L * 1024 * 1024
        val photoBytes = ByteArray(1024)
        val photoUrl = "https://s3.amazonaws.com/pets/user-123/pet-123/buddy.jpg"
        val photo = PhotoUploadData(
            fileName = "buddy.jpg",
            contentType = "image/jpeg",
            fileSize = photoSize,
            fileBytes = photoBytes
        )

        val savedPetWithoutPhoto = Pet(
            id = "pet-123",
            name = "Buddy",
            species = Species.DOG,
            breed = "Golden Retriever",
            age = 3,
            owner = userId,
            registrationDate = LocalDate.now()
        )
        val savedPetWithPhoto = savedPetWithoutPhoto.copy(photoUrl = photoUrl)

        `when`(petPersistenceGateway.countByOwner(userId)).thenReturn(Mono.just(0L))
        `when`(petPersistenceGateway.save(any())).thenReturn(
            Mono.just(savedPetWithoutPhoto),
            Mono.just(savedPetWithPhoto)
        )
        `when`(photoStorageGateway.uploadPhoto(eq(userId), eq("pet-123"), any()))
            .thenReturn(Mono.just(photoUrl))

        val command = RegisterPetCommand(
            userId = userId,
            name = "Buddy",
            species = Species.DOG,
            breed = "Golden Retriever",
            age = 3,
            birthdate = null,
            weight = null,
            nickname = null,
            photo = photo
        )

        StepVerifier.create(registerPetUseCase.execute(command))
            .expectNext(savedPetWithPhoto)
            .verifyComplete()

        verify(photoStorageGateway).uploadPhoto(any(), any(), any())
    }

    // Photo upload error tests
    @Test
    fun `should propagate PhotoUploadException when photo upload fails`() {
        val userId = "user-123"
        val photoBytes = ByteArray(1024)
        val photo = PhotoUploadData(
            fileName = "buddy.jpg",
            contentType = "image/jpeg",
            fileSize = photoBytes.size.toLong(),
            fileBytes = photoBytes
        )

        val savedPetWithoutPhoto = Pet(
            id = "pet-123",
            name = "Buddy",
            species = Species.DOG,
            breed = "Golden Retriever",
            age = 3,
            owner = userId,
            registrationDate = LocalDate.now()
        )

        val uploadError = PhotoUploadException("S3 upload failed", RuntimeException("Connection timeout"))

        `when`(petPersistenceGateway.countByOwner(userId)).thenReturn(Mono.just(0L))
        `when`(petPersistenceGateway.save(any())).thenReturn(Mono.just(savedPetWithoutPhoto))
        `when`(photoStorageGateway.uploadPhoto(any(), any(), any()))
            .thenReturn(Mono.error(uploadError))

        val command = RegisterPetCommand(
            userId = userId,
            name = "Buddy",
            species = Species.DOG,
            breed = "Golden Retriever",
            age = 3,
            birthdate = null,
            weight = null,
            nickname = null,
            photo = photo
        )

        StepVerifier.create(registerPetUseCase.execute(command))
            .expectErrorMatches { throwable ->
                throwable is PhotoUploadException && throwable.message == "S3 upload failed"
            }
            .verify()

        verify(photoStorageGateway).uploadPhoto(eq(userId), eq("pet-123"), any())
    }

    // Persistence error tests
    @Test
    fun `should propagate error when countByOwner fails`() {
        val userId = "user-123"
        val countError = RuntimeException("Database connection failed")

        `when`(petPersistenceGateway.countByOwner(userId)).thenReturn(Mono.error(countError))

        val command = RegisterPetCommand(
            userId = userId,
            name = "Buddy",
            species = Species.DOG,
            breed = "Golden Retriever",
            age = 3,
            birthdate = null,
            weight = null,
            nickname = null,
            photo = null
        )

        StepVerifier.create(registerPetUseCase.execute(command))
            .expectErrorMatches { throwable ->
                throwable is RuntimeException && throwable.message == "Database connection failed"
            }
            .verify()

        verify(petPersistenceGateway).countByOwner(userId)
        verify(petPersistenceGateway, never()).save(any())
    }

    @Test
    fun `should propagate error when initial save fails`() {
        val userId = "user-123"
        val saveError = RuntimeException("Database constraint violation")

        `when`(petPersistenceGateway.countByOwner(userId)).thenReturn(Mono.just(0L))
        `when`(petPersistenceGateway.save(any())).thenReturn(Mono.error(saveError))

        val command = RegisterPetCommand(
            userId = userId,
            name = "Buddy",
            species = Species.DOG,
            breed = "Golden Retriever",
            age = 3,
            birthdate = null,
            weight = null,
            nickname = null,
            photo = null
        )

        StepVerifier.create(registerPetUseCase.execute(command))
            .expectErrorMatches { throwable ->
                throwable is RuntimeException && throwable.message == "Database constraint violation"
            }
            .verify()

        verify(petPersistenceGateway).save(any())
    }

    @Test
    fun `should propagate error when second save after photo upload fails`() {
        val userId = "user-123"
        val photoBytes = ByteArray(1024)
        val photo = PhotoUploadData(
            fileName = "buddy.jpg",
            contentType = "image/jpeg",
            fileSize = photoBytes.size.toLong(),
            fileBytes = photoBytes
        )
        val photoUrl = "https://s3.amazonaws.com/pets/user-123/pet-123/buddy.jpg"

        val savedPetWithoutPhoto = Pet(
            id = "pet-123",
            name = "Buddy",
            species = Species.DOG,
            breed = "Golden Retriever",
            age = 3,
            owner = userId,
            registrationDate = LocalDate.now()
        )

        val saveError = RuntimeException("Database constraint violation on second save")

        `when`(petPersistenceGateway.countByOwner(userId)).thenReturn(Mono.just(0L))
        `when`(petPersistenceGateway.save(any())).thenReturn(
            Mono.just(savedPetWithoutPhoto),
            Mono.error(saveError)
        )
        `when`(photoStorageGateway.uploadPhoto(any(), any(), any()))
            .thenReturn(Mono.just(photoUrl))

        val command = RegisterPetCommand(
            userId = userId,
            name = "Buddy",
            species = Species.DOG,
            breed = "Golden Retriever",
            age = 3,
            birthdate = null,
            weight = null,
            nickname = null,
            photo = photo
        )

        StepVerifier.create(registerPetUseCase.execute(command))
            .expectErrorMatches { throwable ->
                throwable is RuntimeException && throwable.message == "Database constraint violation on second save"
            }
            .verify()

        verify(petPersistenceGateway, times(2)).save(any())
        verify(photoStorageGateway).uploadPhoto(any(), any(), any())
    }
}
