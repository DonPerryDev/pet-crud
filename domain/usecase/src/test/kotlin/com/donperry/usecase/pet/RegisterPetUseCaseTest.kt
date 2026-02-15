package com.donperry.usecase.pet

import com.donperry.model.exception.PetLimitExceededException
import com.donperry.model.exception.PhotoSizeExceededException
import com.donperry.model.exception.PhotoUploadException
import com.donperry.model.exception.ValidationException
import com.donperry.model.pet.Pet
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
        // Given
        val userId = "user-123"
        val name = "Buddy"
        val species = Species.DOG
        val breed = "Golden Retriever"
        val age = 3

        val savedPet = Pet(
            id = "pet-123",
            name = name,
            species = species,
            breed = breed,
            age = age,
            birthdate = null,
            weight = null,
            nickname = null,
            owner = userId,
            registrationDate = LocalDate.now(),
            photoUrl = null
        )

        `when`(petPersistenceGateway.countByOwner(userId)).thenReturn(Mono.just(0L))
        `when`(petPersistenceGateway.save(any())).thenReturn(Mono.just(savedPet))

        // When
        val result = registerPetUseCase.execute(
            userId = userId,
            name = name,
            species = species,
            breed = breed,
            age = age,
            birthdate = null,
            weight = null,
            nickname = null,
            photoFileName = null,
            photoContentType = null,
            photoBytes = null,
            photoSize = null
        )

        // Then
        StepVerifier.create(result)
            .expectNext(savedPet)
            .verifyComplete()

        verify(petPersistenceGateway).countByOwner(userId)
        verify(petPersistenceGateway).save(any())
        verify(photoStorageGateway, never()).uploadPhoto(any(), any(), any(), any(), any(), any())
    }

    @Test
    fun `should register pet with all optional fields populated`() {
        // Given
        val userId = "user-123"
        val name = "Buddy"
        val species = Species.DOG
        val breed = "Golden Retriever"
        val age = 3
        val birthdate = LocalDate.of(2020, 5, 15)
        val weight = BigDecimal("30.0")
        val nickname = "Bud"

        val savedPet = Pet(
            id = "pet-123",
            name = name,
            species = species,
            breed = breed,
            age = age,
            birthdate = birthdate,
            weight = weight,
            nickname = nickname,
            owner = userId,
            registrationDate = LocalDate.now(),
            photoUrl = null
        )

        `when`(petPersistenceGateway.countByOwner(userId)).thenReturn(Mono.just(0L))
        `when`(petPersistenceGateway.save(any())).thenReturn(Mono.just(savedPet))

        // When
        val result = registerPetUseCase.execute(
            userId = userId,
            name = name,
            species = species,
            breed = breed,
            age = age,
            birthdate = birthdate,
            weight = weight,
            nickname = nickname,
            photoFileName = null,
            photoContentType = null,
            photoBytes = null,
            photoSize = null
        )

        // Then
        StepVerifier.create(result)
            .expectNext(savedPet)
            .verifyComplete()

        val petCaptor = argumentCaptor<Pet>()
        verify(petPersistenceGateway).save(petCaptor.capture())

        val capturedPet = petCaptor.firstValue
        assertEquals(name, capturedPet.name)
        assertEquals(species, capturedPet.species)
        assertEquals(breed, capturedPet.breed)
        assertEquals(age, capturedPet.age)
        assertEquals(birthdate, capturedPet.birthdate)
        assertEquals(weight, capturedPet.weight)
        assertEquals(nickname, capturedPet.nickname)
        assertEquals(userId, capturedPet.owner)
        assertNull(capturedPet.id)
        assertEquals(LocalDate.now(), capturedPet.registrationDate)
    }

    @Test
    fun `should register pet with photo and update photoUrl`() {
        // Given
        val userId = "user-123"
        val name = "Buddy"
        val species = Species.DOG
        val breed = "Golden Retriever"
        val age = 3
        val photoFileName = "buddy.jpg"
        val photoContentType = "image/jpeg"
        val photoBytes = ByteArray(1024) { it.toByte() }
        val photoSize = photoBytes.size.toLong()
        val photoUrl = "https://s3.amazonaws.com/pets/user-123/pet-123/buddy.jpg"

        val savedPetWithoutPhoto = Pet(
            id = "pet-123",
            name = name,
            species = species,
            breed = breed,
            age = age,
            birthdate = null,
            weight = null,
            nickname = null,
            owner = userId,
            registrationDate = LocalDate.now(),
            photoUrl = null
        )

        val savedPetWithPhoto = savedPetWithoutPhoto.copy(photoUrl = photoUrl)

        `when`(petPersistenceGateway.countByOwner(userId)).thenReturn(Mono.just(0L))
        `when`(petPersistenceGateway.save(any())).thenReturn(
            Mono.just(savedPetWithoutPhoto),
            Mono.just(savedPetWithPhoto)
        )
        `when`(photoStorageGateway.uploadPhoto(userId, "pet-123", photoFileName, photoContentType, photoSize, photoBytes))
            .thenReturn(Mono.just(photoUrl))

        // When
        val result = registerPetUseCase.execute(
            userId = userId,
            name = name,
            species = species,
            breed = breed,
            age = age,
            birthdate = null,
            weight = null,
            nickname = null,
            photoFileName = photoFileName,
            photoContentType = photoContentType,
            photoBytes = photoBytes,
            photoSize = photoSize
        )

        // Then
        StepVerifier.create(result)
            .expectNext(savedPetWithPhoto)
            .verifyComplete()

        verify(petPersistenceGateway, times(2)).save(any())
        verify(photoStorageGateway).uploadPhoto(userId, "pet-123", photoFileName, photoContentType, photoSize, photoBytes)
    }

    @Test
    fun `should handle null optional fields correctly`() {
        // Given
        val userId = "user-456"
        val name = "Mittens"
        val species = Species.CAT
        val breed: String? = null
        val age = 2

        val savedPet = Pet(
            id = "pet-456",
            name = name,
            species = species,
            breed = breed,
            age = age,
            birthdate = null,
            weight = null,
            nickname = null,
            owner = userId,
            registrationDate = LocalDate.now(),
            photoUrl = null
        )

        `when`(petPersistenceGateway.countByOwner(userId)).thenReturn(Mono.just(1L))
        `when`(petPersistenceGateway.save(any())).thenReturn(Mono.just(savedPet))

        // When
        val result = registerPetUseCase.execute(
            userId = userId,
            name = name,
            species = species,
            breed = breed,
            age = age,
            birthdate = null,
            weight = null,
            nickname = null,
            photoFileName = null,
            photoContentType = null,
            photoBytes = null,
            photoSize = null
        )

        // Then
        StepVerifier.create(result)
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
        // Given
        val userId = "user-000"
        val name = "Puppy"
        val species = Species.DOG
        val breed = "Mixed"
        val age = 0

        val savedPet = Pet(
            id = "pet-000",
            name = name,
            species = species,
            breed = breed,
            age = age,
            birthdate = null,
            weight = null,
            nickname = null,
            owner = userId,
            registrationDate = LocalDate.now(),
            photoUrl = null
        )

        `when`(petPersistenceGateway.countByOwner(userId)).thenReturn(Mono.just(0L))
        `when`(petPersistenceGateway.save(any())).thenReturn(Mono.just(savedPet))

        // When
        val result = registerPetUseCase.execute(
            userId = userId,
            name = name,
            species = species,
            breed = breed,
            age = age,
            birthdate = null,
            weight = null,
            nickname = null,
            photoFileName = null,
            photoContentType = null,
            photoBytes = null,
            photoSize = null
        )

        // Then
        StepVerifier.create(result)
            .expectNext(savedPet)
            .verifyComplete()

        val petCaptor = argumentCaptor<Pet>()
        verify(petPersistenceGateway).save(petCaptor.capture())
        assertEquals(0, petCaptor.firstValue.age)
    }

    // Validation error tests
    @Test
    fun `should throw ValidationException when userId is blank`() {
        // Given
        `when`(petPersistenceGateway.countByOwner(any())).thenReturn(Mono.just(0L))
        // When
        val result = registerPetUseCase.execute(
            userId = "",
            name = "Buddy",
            species = Species.DOG,
            breed = "Golden Retriever",
            age = 3,
            birthdate = null,
            weight = null,
            nickname = null,
            photoFileName = null,
            photoContentType = null,
            photoBytes = null,
            photoSize = null
        )

        // Then
        StepVerifier.create(result)
            .expectErrorMatches { throwable ->
                throwable is ValidationException && throwable.message == "User ID cannot be blank"
            }
            .verify()

        verify(petPersistenceGateway, never()).save(any())
    }

    @Test
    fun `should throw ValidationException when name is blank`() {
        // Given
        `when`(petPersistenceGateway.countByOwner(any())).thenReturn(Mono.just(0L))
        // When
        val result = registerPetUseCase.execute(
            userId = "user-123",
            name = "",
            species = Species.DOG,
            breed = "Golden Retriever",
            age = 3,
            birthdate = null,
            weight = null,
            nickname = null,
            photoFileName = null,
            photoContentType = null,
            photoBytes = null,
            photoSize = null
        )

        // Then
        StepVerifier.create(result)
            .expectErrorMatches { throwable ->
                throwable is ValidationException && throwable.message == "Pet name cannot be blank"
            }
            .verify()

        verify(petPersistenceGateway, never()).save(any())
    }

    @Test
    fun `should throw ValidationException when age is negative`() {
        // Given
        `when`(petPersistenceGateway.countByOwner(any())).thenReturn(Mono.just(0L))
        // When
        val result = registerPetUseCase.execute(
            userId = "user-123",
            name = "Buddy",
            species = Species.DOG,
            breed = "Golden Retriever",
            age = -1,
            birthdate = null,
            weight = null,
            nickname = null,
            photoFileName = null,
            photoContentType = null,
            photoBytes = null,
            photoSize = null
        )

        // Then
        StepVerifier.create(result)
            .expectErrorMatches { throwable ->
                throwable is ValidationException && throwable.message == "Pet age must be zero or greater"
            }
            .verify()

        verify(petPersistenceGateway, never()).save(any())
    }

    @Test
    fun `should throw ValidationException when birthdate is in future`() {
        // Given
        val futureBirthdate = LocalDate.now().plusDays(1)

        // Given
        `when`(petPersistenceGateway.countByOwner(any())).thenReturn(Mono.just(0L))
        // When
        val result = registerPetUseCase.execute(
            userId = "user-123",
            name = "Buddy",
            species = Species.DOG,
            breed = "Golden Retriever",
            age = 3,
            birthdate = futureBirthdate,
            weight = null,
            nickname = null,
            photoFileName = null,
            photoContentType = null,
            photoBytes = null,
            photoSize = null
        )

        // Then
        StepVerifier.create(result)
            .expectErrorMatches { throwable ->
                throwable is ValidationException && throwable.message == "Pet birthdate cannot be in the future"
            }
            .verify()

        verify(petPersistenceGateway, never()).save(any())
    }

    @Test
    fun `should throw ValidationException when weight is zero`() {
        // Given
        `when`(petPersistenceGateway.countByOwner(any())).thenReturn(Mono.just(0L))
        // When
        val result = registerPetUseCase.execute(
            userId = "user-123",
            name = "Buddy",
            species = Species.DOG,
            breed = "Golden Retriever",
            age = 3,
            birthdate = null,
            weight = BigDecimal.ZERO,
            nickname = null,
            photoFileName = null,
            photoContentType = null,
            photoBytes = null,
            photoSize = null
        )

        // Then
        StepVerifier.create(result)
            .expectErrorMatches { throwable ->
                throwable is ValidationException && throwable.message == "Pet weight must be greater than zero"
            }
            .verify()

        verify(petPersistenceGateway, never()).save(any())
    }

    @Test
    fun `should throw ValidationException when weight is negative`() {
        // Given
        `when`(petPersistenceGateway.countByOwner(any())).thenReturn(Mono.just(0L))
        // When
        val result = registerPetUseCase.execute(
            userId = "user-123",
            name = "Buddy",
            species = Species.DOG,
            breed = "Golden Retriever",
            age = 3,
            birthdate = null,
            weight = BigDecimal("-5.0"),
            nickname = null,
            photoFileName = null,
            photoContentType = null,
            photoBytes = null,
            photoSize = null
        )

        // Then
        StepVerifier.create(result)
            .expectErrorMatches { throwable ->
                throwable is ValidationException && throwable.message == "Pet weight must be greater than zero"
            }
            .verify()

        verify(petPersistenceGateway, never()).save(any())
    }

    // Pet limit tests
    @Test
    fun `should throw PetLimitExceededException when user has 10 pets`() {
        // Given
        val userId = "user-123"

        `when`(petPersistenceGateway.countByOwner(userId)).thenReturn(Mono.just(10L))

        // When
        val result = registerPetUseCase.execute(
            userId = userId,
            name = "Buddy",
            species = Species.DOG,
            breed = "Golden Retriever",
            age = 3,
            birthdate = null,
            weight = null,
            nickname = null,
            photoFileName = null,
            photoContentType = null,
            photoBytes = null,
            photoSize = null
        )

        // Then
        StepVerifier.create(result)
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
        // Given
        val userId = "user-123"
        val savedPet = Pet(
            id = "pet-123",
            name = "Buddy",
            species = Species.DOG,
            breed = "Golden Retriever",
            age = 3,
            birthdate = null,
            weight = null,
            nickname = null,
            owner = userId,
            registrationDate = LocalDate.now(),
            photoUrl = null
        )

        `when`(petPersistenceGateway.countByOwner(userId)).thenReturn(Mono.just(9L))
        `when`(petPersistenceGateway.save(any())).thenReturn(Mono.just(savedPet))

        // When
        val result = registerPetUseCase.execute(
            userId = userId,
            name = "Buddy",
            species = Species.DOG,
            breed = "Golden Retriever",
            age = 3,
            birthdate = null,
            weight = null,
            nickname = null,
            photoFileName = null,
            photoContentType = null,
            photoBytes = null,
            photoSize = null
        )

        // Then
        StepVerifier.create(result)
            .expectNext(savedPet)
            .verifyComplete()

        verify(petPersistenceGateway).countByOwner(userId)
        verify(petPersistenceGateway).save(any())
    }

    // Photo validation tests
    @Test
    fun `should throw PhotoSizeExceededException when photo exceeds 5MB`() {
        // Given
        val photoSize = 6L * 1024 * 1024 // 6MB
        `when`(petPersistenceGateway.countByOwner(any())).thenReturn(Mono.just(0L))

        // When
        val result = registerPetUseCase.execute(
            userId = "user-123",
            name = "Buddy",
            species = Species.DOG,
            breed = "Golden Retriever",
            age = 3,
            birthdate = null,
            weight = null,
            nickname = null,
            photoFileName = "buddy.jpg",
            photoContentType = "image/jpeg",
            photoBytes = ByteArray(1024),
            photoSize = photoSize
        )

        // Then
        StepVerifier.create(result)
            .expectErrorMatches { throwable ->
                throwable is PhotoSizeExceededException &&
                throwable.message!!.contains("exceeds maximum allowed size")
            }
            .verify()

        verify(petPersistenceGateway, never()).save(any())
    }

    @Test
    fun `should accept photo of exactly 5MB`() {
        // Given
        val userId = "user-123"
        val photoSize = 5L * 1024 * 1024 // Exactly 5MB
        val photoFileName = "buddy.jpg"
        val photoContentType = "image/jpeg"
        val photoBytes = ByteArray(1024)
        val photoUrl = "https://s3.amazonaws.com/pets/user-123/pet-123/buddy.jpg"

        val savedPetWithoutPhoto = Pet(
            id = "pet-123",
            name = "Buddy",
            species = Species.DOG,
            breed = "Golden Retriever",
            age = 3,
            birthdate = null,
            weight = null,
            nickname = null,
            owner = userId,
            registrationDate = LocalDate.now(),
            photoUrl = null
        )

        val savedPetWithPhoto = savedPetWithoutPhoto.copy(photoUrl = photoUrl)

        `when`(petPersistenceGateway.countByOwner(userId)).thenReturn(Mono.just(0L))
        `when`(petPersistenceGateway.save(any())).thenReturn(
            Mono.just(savedPetWithoutPhoto),
            Mono.just(savedPetWithPhoto)
        )
        `when`(photoStorageGateway.uploadPhoto(userId, "pet-123", photoFileName, photoContentType, photoSize, photoBytes))
            .thenReturn(Mono.just(photoUrl))

        // When
        val result = registerPetUseCase.execute(
            userId = userId,
            name = "Buddy",
            species = Species.DOG,
            breed = "Golden Retriever",
            age = 3,
            birthdate = null,
            weight = null,
            nickname = null,
            photoFileName = photoFileName,
            photoContentType = photoContentType,
            photoBytes = photoBytes,
            photoSize = photoSize
        )

        // Then
        StepVerifier.create(result)
            .expectNext(savedPetWithPhoto)
            .verifyComplete()

        verify(photoStorageGateway).uploadPhoto(any(), any(), any(), any(), any(), any())
    }

    // Photo upload error tests
    @Test
    fun `should propagate PhotoUploadException when photo upload fails`() {
        // Given
        val userId = "user-123"
        val photoFileName = "buddy.jpg"
        val photoContentType = "image/jpeg"
        val photoBytes = ByteArray(1024)
        val photoSize = photoBytes.size.toLong()

        val savedPetWithoutPhoto = Pet(
            id = "pet-123",
            name = "Buddy",
            species = Species.DOG,
            breed = "Golden Retriever",
            age = 3,
            birthdate = null,
            weight = null,
            nickname = null,
            owner = userId,
            registrationDate = LocalDate.now(),
            photoUrl = null
        )

        val uploadError = PhotoUploadException("S3 upload failed", RuntimeException("Connection timeout"))

        `when`(petPersistenceGateway.countByOwner(userId)).thenReturn(Mono.just(0L))
        `when`(petPersistenceGateway.save(any())).thenReturn(Mono.just(savedPetWithoutPhoto))
        `when`(photoStorageGateway.uploadPhoto(any(), any(), any(), any(), any(), any()))
            .thenReturn(Mono.error(uploadError))

        // When
        val result = registerPetUseCase.execute(
            userId = userId,
            name = "Buddy",
            species = Species.DOG,
            breed = "Golden Retriever",
            age = 3,
            birthdate = null,
            weight = null,
            nickname = null,
            photoFileName = photoFileName,
            photoContentType = photoContentType,
            photoBytes = photoBytes,
            photoSize = photoSize
        )

        // Then
        StepVerifier.create(result)
            .expectErrorMatches { throwable ->
                throwable is PhotoUploadException && throwable.message == "S3 upload failed"
            }
            .verify()

        verify(photoStorageGateway).uploadPhoto(userId, "pet-123", photoFileName, photoContentType, photoSize, photoBytes)
    }

    // Persistence error tests
    @Test
    fun `should propagate error when countByOwner fails`() {
        // Given
        val userId = "user-123"
        val countError = RuntimeException("Database connection failed")

        `when`(petPersistenceGateway.countByOwner(userId)).thenReturn(Mono.error(countError))

        // When
        val result = registerPetUseCase.execute(
            userId = userId,
            name = "Buddy",
            species = Species.DOG,
            breed = "Golden Retriever",
            age = 3,
            birthdate = null,
            weight = null,
            nickname = null,
            photoFileName = null,
            photoContentType = null,
            photoBytes = null,
            photoSize = null
        )

        // Then
        StepVerifier.create(result)
            .expectErrorMatches { throwable ->
                throwable is RuntimeException && throwable.message == "Database connection failed"
            }
            .verify()

        verify(petPersistenceGateway).countByOwner(userId)
        verify(petPersistenceGateway, never()).save(any())
    }

    @Test
    fun `should propagate error when initial save fails`() {
        // Given
        val userId = "user-123"
        val saveError = RuntimeException("Database constraint violation")

        `when`(petPersistenceGateway.countByOwner(userId)).thenReturn(Mono.just(0L))
        `when`(petPersistenceGateway.save(any())).thenReturn(Mono.error(saveError))

        // When
        val result = registerPetUseCase.execute(
            userId = userId,
            name = "Buddy",
            species = Species.DOG,
            breed = "Golden Retriever",
            age = 3,
            birthdate = null,
            weight = null,
            nickname = null,
            photoFileName = null,
            photoContentType = null,
            photoBytes = null,
            photoSize = null
        )

        // Then
        StepVerifier.create(result)
            .expectErrorMatches { throwable ->
                throwable is RuntimeException && throwable.message == "Database constraint violation"
            }
            .verify()

        verify(petPersistenceGateway).save(any())
    }

    @Test
    fun `should propagate error when second save after photo upload fails`() {
        // Given
        val userId = "user-123"
        val photoFileName = "buddy.jpg"
        val photoContentType = "image/jpeg"
        val photoBytes = ByteArray(1024)
        val photoSize = photoBytes.size.toLong()
        val photoUrl = "https://s3.amazonaws.com/pets/user-123/pet-123/buddy.jpg"

        val savedPetWithoutPhoto = Pet(
            id = "pet-123",
            name = "Buddy",
            species = Species.DOG,
            breed = "Golden Retriever",
            age = 3,
            birthdate = null,
            weight = null,
            nickname = null,
            owner = userId,
            registrationDate = LocalDate.now(),
            photoUrl = null
        )

        val saveError = RuntimeException("Database constraint violation on second save")

        `when`(petPersistenceGateway.countByOwner(userId)).thenReturn(Mono.just(0L))
        `when`(petPersistenceGateway.save(any())).thenReturn(
            Mono.just(savedPetWithoutPhoto),
            Mono.error(saveError)
        )
        `when`(photoStorageGateway.uploadPhoto(any(), any(), any(), any(), any(), any()))
            .thenReturn(Mono.just(photoUrl))

        // When
        val result = registerPetUseCase.execute(
            userId = userId,
            name = "Buddy",
            species = Species.DOG,
            breed = "Golden Retriever",
            age = 3,
            birthdate = null,
            weight = null,
            nickname = null,
            photoFileName = photoFileName,
            photoContentType = photoContentType,
            photoBytes = photoBytes,
            photoSize = photoSize
        )

        // Then
        StepVerifier.create(result)
            .expectErrorMatches { throwable ->
                throwable is RuntimeException && throwable.message == "Database constraint violation on second save"
            }
            .verify()

        verify(petPersistenceGateway, times(2)).save(any())
        verify(photoStorageGateway).uploadPhoto(any(), any(), any(), any(), any(), any())
    }
}
