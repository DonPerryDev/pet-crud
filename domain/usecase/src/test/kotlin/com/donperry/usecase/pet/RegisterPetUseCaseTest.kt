package com.donperry.usecase.pet

import com.donperry.model.exception.PetLimitExceededException
import com.donperry.model.exception.ValidationException
import com.donperry.model.pet.Pet
import com.donperry.model.pet.RegisterPetCommand
import com.donperry.model.pet.Species
import com.donperry.model.pet.gateway.PetPersistenceGateway
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
import java.math.BigDecimal
import java.time.LocalDate
import kotlin.test.assertEquals
import kotlin.test.assertNull

@ExtendWith(MockitoExtension::class)
class RegisterPetUseCaseTest {

    @Mock
    private lateinit var petPersistenceGateway: PetPersistenceGateway

    @InjectMocks
    private lateinit var registerPetUseCase: RegisterPetUseCase

    // Happy path tests
    @Test
    fun `should register pet when all required fields valid`() {
        val userId = "user-123"
        val savedPet = Pet(
            id = "pet-123",
            name = "Buddy",
            species = Species.DOG,
            breed = "Golden Retriever",
            age = 3,
            owner = userId,
            registrationDate = LocalDate.now(),
            photoUrl = null
        )

        whenever(petPersistenceGateway.countByOwner(userId)).thenReturn(Mono.just(0L))
        whenever(petPersistenceGateway.save(any())).thenReturn(Mono.just(savedPet))

        val command = RegisterPetCommand(
            userId = userId,
            name = "Buddy",
            species = Species.DOG,
            breed = "Golden Retriever",
            age = 3,
            birthdate = null,
            weight = null,
            nickname = null
        )

        StepVerifier.create(registerPetUseCase.execute(command))
            .expectNext(savedPet)
            .verifyComplete()

        verify(petPersistenceGateway).countByOwner(userId)
        verify(petPersistenceGateway).save(any())
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
            registrationDate = LocalDate.now(),
            photoUrl = null
        )

        whenever(petPersistenceGateway.countByOwner(userId)).thenReturn(Mono.just(0L))
        whenever(petPersistenceGateway.save(any())).thenReturn(Mono.just(savedPet))

        val command = RegisterPetCommand(
            userId = userId,
            name = "Buddy",
            species = Species.DOG,
            breed = "Golden Retriever",
            age = 3,
            birthdate = birthdate,
            weight = weight,
            nickname = "Bud"
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
        assertNull(capturedPet.photoUrl)
        assertEquals(LocalDate.now(), capturedPet.registrationDate)
    }

    @Test
    fun `should register pet with all optional fields null`() {
        val userId = "user-456"

        val savedPet = Pet(
            id = "pet-456",
            name = "Mittens",
            species = Species.CAT,
            breed = null,
            age = 2,
            owner = userId,
            registrationDate = LocalDate.now(),
            photoUrl = null
        )

        whenever(petPersistenceGateway.countByOwner(userId)).thenReturn(Mono.just(1L))
        whenever(petPersistenceGateway.save(any())).thenReturn(Mono.just(savedPet))

        val command = RegisterPetCommand(
            userId = userId,
            name = "Mittens",
            species = Species.CAT,
            breed = null,
            age = 2,
            birthdate = null,
            weight = null,
            nickname = null
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
        assertNull(capturedPet.photoUrl)
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
            registrationDate = LocalDate.now(),
            photoUrl = null
        )

        whenever(petPersistenceGateway.countByOwner(userId)).thenReturn(Mono.just(0L))
        whenever(petPersistenceGateway.save(any())).thenReturn(Mono.just(savedPet))

        val command = RegisterPetCommand(
            userId = userId,
            name = "Puppy",
            species = Species.DOG,
            breed = "Mixed",
            age = 0,
            birthdate = null,
            weight = null,
            nickname = null
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
            nickname = null
        )

        StepVerifier.create(registerPetUseCase.execute(command))
            .expectErrorMatches { throwable ->
                throwable is ValidationException && throwable.message == "User ID cannot be blank"
            }
            .verify()

        verify(petPersistenceGateway, never()).save(any())
        verify(petPersistenceGateway, never()).countByOwner(any())
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
            nickname = null
        )

        StepVerifier.create(registerPetUseCase.execute(command))
            .expectErrorMatches { throwable ->
                throwable is ValidationException && throwable.message == "Pet name cannot be blank"
            }
            .verify()

        verify(petPersistenceGateway, never()).save(any())
        verify(petPersistenceGateway, never()).countByOwner(any())
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
            nickname = null
        )

        StepVerifier.create(registerPetUseCase.execute(command))
            .expectErrorMatches { throwable ->
                throwable is ValidationException && throwable.message == "Pet age must be zero or greater"
            }
            .verify()

        verify(petPersistenceGateway, never()).save(any())
        verify(petPersistenceGateway, never()).countByOwner(any())
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
            nickname = null
        )

        StepVerifier.create(registerPetUseCase.execute(command))
            .expectErrorMatches { throwable ->
                throwable is ValidationException && throwable.message == "Pet birthdate cannot be in the future"
            }
            .verify()

        verify(petPersistenceGateway, never()).save(any())
        verify(petPersistenceGateway, never()).countByOwner(any())
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
            nickname = null
        )

        StepVerifier.create(registerPetUseCase.execute(command))
            .expectErrorMatches { throwable ->
                throwable is ValidationException && throwable.message == "Pet weight must be greater than zero"
            }
            .verify()

        verify(petPersistenceGateway, never()).save(any())
        verify(petPersistenceGateway, never()).countByOwner(any())
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
            nickname = null
        )

        StepVerifier.create(registerPetUseCase.execute(command))
            .expectErrorMatches { throwable ->
                throwable is ValidationException && throwable.message == "Pet weight must be greater than zero"
            }
            .verify()

        verify(petPersistenceGateway, never()).save(any())
        verify(petPersistenceGateway, never()).countByOwner(any())
    }

    @Test
    fun `should throw ValidationException with multiple errors when multiple fields invalid`() {
        val command = RegisterPetCommand(
            userId = "",
            name = "",
            species = Species.DOG,
            breed = "Golden Retriever",
            age = -1,
            birthdate = null,
            weight = null,
            nickname = null
        )

        StepVerifier.create(registerPetUseCase.execute(command))
            .expectErrorMatches { throwable ->
                throwable is ValidationException &&
                throwable.message!!.contains("User ID cannot be blank") &&
                throwable.message!!.contains("Pet name cannot be blank") &&
                throwable.message!!.contains("Pet age must be zero or greater")
            }
            .verify()

        verify(petPersistenceGateway, never()).save(any())
        verify(petPersistenceGateway, never()).countByOwner(any())
    }

    // Pet limit tests
    @Test
    fun `should throw PetLimitExceededException when user has 10 pets`() {
        val userId = "user-123"

        whenever(petPersistenceGateway.countByOwner(userId)).thenReturn(Mono.just(10L))

        val command = RegisterPetCommand(
            userId = userId,
            name = "Buddy",
            species = Species.DOG,
            breed = "Golden Retriever",
            age = 3,
            birthdate = null,
            weight = null,
            nickname = null
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
            registrationDate = LocalDate.now(),
            photoUrl = null
        )

        whenever(petPersistenceGateway.countByOwner(userId)).thenReturn(Mono.just(9L))
        whenever(petPersistenceGateway.save(any())).thenReturn(Mono.just(savedPet))

        val command = RegisterPetCommand(
            userId = userId,
            name = "Buddy",
            species = Species.DOG,
            breed = "Golden Retriever",
            age = 3,
            birthdate = null,
            weight = null,
            nickname = null
        )

        StepVerifier.create(registerPetUseCase.execute(command))
            .expectNext(savedPet)
            .verifyComplete()

        verify(petPersistenceGateway).countByOwner(userId)
        verify(petPersistenceGateway).save(any())
    }

    @Test
    fun `should throw PetLimitExceededException when user exceeds 10 pets`() {
        val userId = "user-123"

        whenever(petPersistenceGateway.countByOwner(userId)).thenReturn(Mono.just(15L))

        val command = RegisterPetCommand(
            userId = userId,
            name = "Buddy",
            species = Species.DOG,
            breed = "Golden Retriever",
            age = 3,
            birthdate = null,
            weight = null,
            nickname = null
        )

        StepVerifier.create(registerPetUseCase.execute(command))
            .expectError(PetLimitExceededException::class.java)
            .verify()

        verify(petPersistenceGateway).countByOwner(userId)
        verify(petPersistenceGateway, never()).save(any())
    }

    // Persistence error tests
    @Test
    fun `should propagate error when countByOwner fails`() {
        val userId = "user-123"
        val countError = RuntimeException("Database connection failed")

        whenever(petPersistenceGateway.countByOwner(userId)).thenReturn(Mono.error(countError))

        val command = RegisterPetCommand(
            userId = userId,
            name = "Buddy",
            species = Species.DOG,
            breed = "Golden Retriever",
            age = 3,
            birthdate = null,
            weight = null,
            nickname = null
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
    fun `should propagate error when save fails`() {
        val userId = "user-123"
        val saveError = RuntimeException("Database constraint violation")

        whenever(petPersistenceGateway.countByOwner(userId)).thenReturn(Mono.just(0L))
        whenever(petPersistenceGateway.save(any())).thenReturn(Mono.error(saveError))

        val command = RegisterPetCommand(
            userId = userId,
            name = "Buddy",
            species = Species.DOG,
            breed = "Golden Retriever",
            age = 3,
            birthdate = null,
            weight = null,
            nickname = null
        )

        StepVerifier.create(registerPetUseCase.execute(command))
            .expectErrorMatches { throwable ->
                throwable is RuntimeException && throwable.message == "Database constraint violation"
            }
            .verify()

        verify(petPersistenceGateway).save(any())
    }

    @Test
    fun `should create pet with today registration date`() {
        val userId = "user-123"
        val today = LocalDate.now()

        val savedPet = Pet(
            id = "pet-123",
            name = "Buddy",
            species = Species.DOG,
            breed = "Golden Retriever",
            age = 3,
            owner = userId,
            registrationDate = today,
            photoUrl = null
        )

        whenever(petPersistenceGateway.countByOwner(userId)).thenReturn(Mono.just(0L))
        whenever(petPersistenceGateway.save(any())).thenReturn(Mono.just(savedPet))

        val command = RegisterPetCommand(
            userId = userId,
            name = "Buddy",
            species = Species.DOG,
            breed = "Golden Retriever",
            age = 3,
            birthdate = null,
            weight = null,
            nickname = null
        )

        StepVerifier.create(registerPetUseCase.execute(command))
            .expectNext(savedPet)
            .verifyComplete()

        val petCaptor = argumentCaptor<Pet>()
        verify(petPersistenceGateway).save(petCaptor.capture())
        assertEquals(today, petCaptor.firstValue.registrationDate)
    }

    @Test
    fun `should create pet with null photoUrl by default`() {
        val userId = "user-123"

        val savedPet = Pet(
            id = "pet-123",
            name = "Buddy",
            species = Species.DOG,
            breed = "Golden Retriever",
            age = 3,
            owner = userId,
            registrationDate = LocalDate.now(),
            photoUrl = null
        )

        whenever(petPersistenceGateway.countByOwner(userId)).thenReturn(Mono.just(0L))
        whenever(petPersistenceGateway.save(any())).thenReturn(Mono.just(savedPet))

        val command = RegisterPetCommand(
            userId = userId,
            name = "Buddy",
            species = Species.DOG,
            breed = "Golden Retriever",
            age = 3,
            birthdate = null,
            weight = null,
            nickname = null
        )

        StepVerifier.create(registerPetUseCase.execute(command))
            .expectNext(savedPet)
            .verifyComplete()

        val petCaptor = argumentCaptor<Pet>()
        verify(petPersistenceGateway).save(petCaptor.capture())
        assertNull(petCaptor.firstValue.photoUrl)
    }
}
