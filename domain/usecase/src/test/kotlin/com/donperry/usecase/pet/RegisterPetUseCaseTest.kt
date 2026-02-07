package com.donperry.usecase.pet

import com.donperry.model.pet.Pet
import com.donperry.model.pet.gateway.PetPersistenceGateway
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.verify
import reactor.core.publisher.Mono
import reactor.test.StepVerifier
import java.time.LocalDate
import kotlin.test.assertEquals
import kotlin.test.assertNull

@ExtendWith(MockitoExtension::class)
class RegisterPetUseCaseTest {

    @Mock
    private lateinit var petPersistenceGateway: PetPersistenceGateway

    private lateinit var registerPetUseCase: RegisterPetUseCase

    @BeforeEach
    fun setUp() {
        registerPetUseCase = RegisterPetUseCase(petPersistenceGateway)
    }

    @Test
    fun `execute should create pet with all fields and save successfully`() {
        // Given
        val name = "Buddy"
        val species = "Dog"
        val breed = "Golden Retriever"
        val age = 3
        val owner = "John Doe"
        
        val savedPet = Pet(
            id = "pet-123",
            name = name,
            species = species,
            breed = breed,
            age = age,
            owner = owner,
            registrationDate = LocalDate.now()
        )

        `when`(petPersistenceGateway.save(any())).thenReturn(Mono.just(savedPet))

        // When
        val result = registerPetUseCase.execute(name, species, breed, age, owner)

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
        assertEquals(owner, capturedPet.owner)
        assertNull(capturedPet.id) // Should be null before persistence
        assertEquals(LocalDate.now(), capturedPet.registrationDate)
    }

    @Test
    fun `execute should handle null breed correctly`() {
        // Given
        val name = "Mittens"
        val species = "Cat"
        val breed: String? = null
        val age = 2
        val owner = "Jane Smith"
        
        val savedPet = Pet(
            id = "pet-456",
            name = name,
            species = species,
            breed = breed,
            age = age,
            owner = owner,
            registrationDate = LocalDate.now()
        )

        `when`(petPersistenceGateway.save(any())).thenReturn(Mono.just(savedPet))

        // When
        val result = registerPetUseCase.execute(name, species, breed, age, owner)

        // Then
        StepVerifier.create(result)
            .expectNext(savedPet)
            .verifyComplete()

        val petCaptor = argumentCaptor<Pet>()
        verify(petPersistenceGateway).save(petCaptor.capture())
        
        val capturedPet = petCaptor.firstValue
        assertNull(capturedPet.breed)
    }

    @Test
    fun `execute should set registration date to current date`() {
        // Given
        val name = "Rex"
        val species = "Dog"
        val breed = "German Shepherd"
        val age = 5
        val owner = "Bob Wilson"
        val today = LocalDate.now()
        
        val savedPet = Pet(
            id = "pet-789",
            name = name,
            species = species,
            breed = breed,
            age = age,
            owner = owner,
            registrationDate = today
        )

        `when`(petPersistenceGateway.save(any())).thenReturn(Mono.just(savedPet))

        // When
        val result = registerPetUseCase.execute(name, species, breed, age, owner)

        // Then
        StepVerifier.create(result)
            .expectNext(savedPet)
            .verifyComplete()

        val petCaptor = argumentCaptor<Pet>()
        verify(petPersistenceGateway).save(petCaptor.capture())
        
        val capturedPet = petCaptor.firstValue
        assertEquals(today, capturedPet.registrationDate)
    }

    @Test
    fun `execute should propagate persistence gateway errors`() {
        // Given
        val name = "Buddy"
        val species = "Dog"
        val breed = "Golden Retriever"
        val age = 3
        val owner = "John Doe"
        
        val persistenceError = RuntimeException("Database connection failed")
        `when`(petPersistenceGateway.save(any())).thenReturn(Mono.error(persistenceError))

        // When
        val result = registerPetUseCase.execute(name, species, breed, age, owner)

        // Then
        StepVerifier.create(result)
            .expectErrorMatches { throwable ->
                throwable is RuntimeException && throwable.message == "Database connection failed"
            }
            .verify()

        verify(petPersistenceGateway).save(any())
    }

    @Test
    fun `execute should handle empty strings for optional fields`() {
        // Given
        val name = "Fluffy"
        val species = "Cat"
        val breed = "" // Empty string
        val age = 1
        val owner = "Alice Brown"
        
        val savedPet = Pet(
            id = "pet-999",
            name = name,
            species = species,
            breed = breed,
            age = age,
            owner = owner,
            registrationDate = LocalDate.now()
        )

        `when`(petPersistenceGateway.save(any())).thenReturn(Mono.just(savedPet))

        // When
        val result = registerPetUseCase.execute(name, species, breed, age, owner)

        // Then
        StepVerifier.create(result)
            .expectNext(savedPet)
            .verifyComplete()

        val petCaptor = argumentCaptor<Pet>()
        verify(petPersistenceGateway).save(petCaptor.capture())
        
        val capturedPet = petCaptor.firstValue
        assertEquals("", capturedPet.breed)
    }

    @Test
    fun `execute should handle zero age`() {
        // Given
        val name = "Puppy"
        val species = "Dog"
        val breed = "Mixed"
        val age = 0
        val owner = "Charlie Green"
        
        val savedPet = Pet(
            id = "pet-000",
            name = name,
            species = species,
            breed = breed,
            age = age,
            owner = owner,
            registrationDate = LocalDate.now()
        )

        `when`(petPersistenceGateway.save(any())).thenReturn(Mono.just(savedPet))

        // When
        val result = registerPetUseCase.execute(name, species, breed, age, owner)

        // Then
        StepVerifier.create(result)
            .expectNext(savedPet)
            .verifyComplete()

        val petCaptor = argumentCaptor<Pet>()
        verify(petPersistenceGateway).save(petCaptor.capture())
        
        val capturedPet = petCaptor.firstValue
        assertEquals(0, capturedPet.age)
    }
}