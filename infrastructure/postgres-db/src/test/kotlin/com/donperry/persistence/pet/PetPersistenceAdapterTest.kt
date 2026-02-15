package com.donperry.persistence.pet

import com.donperry.model.pet.Pet
import com.donperry.model.pet.Species
import com.donperry.persistence.pet.entities.PetData
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
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertNull

@ExtendWith(MockitoExtension::class)
class PetPersistenceAdapterTest {

    @Mock
    private lateinit var petRepository: PetRepository

    private lateinit var petPersistenceAdapter: PetPersistenceAdapter

    @BeforeEach
    fun setUp() {
        petPersistenceAdapter = PetPersistenceAdapter(petRepository)
    }

    @Test
    fun `save should convert model to entity, save and convert back to model`() {
        // Given
        val petModel = Pet(
            id = null, // New pet without ID
            name = "Buddy",
            species = Species.DOG,
            breed = "Golden Retriever",
            age = 3,
            birthdate = null,
            weight = null,
            nickname = null,
            owner = "John Doe",
            registrationDate = LocalDate.now(),
            photoUrl = null
        )

        val savedPetData = PetData(
            id = UUID.randomUUID(),
            name = "Buddy",
            species = "DOG",
            breed = "Golden Retriever",
            age = 3,
            birthdate = null,
            weight = null,
            nickname = null,
            owner = "John Doe",
            registrationDate = LocalDate.now(),
            photoUrl = null
        )

        `when`(petRepository.save(any<PetData>())).thenReturn(Mono.just(savedPetData))

        // When
        val result = petPersistenceAdapter.save(petModel)

        // Then
        StepVerifier.create(result)
            .expectNextMatches { savedPet ->
                savedPet.id == savedPetData.id.toString() &&
                savedPet.name == "Buddy" &&
                savedPet.species == Species.DOG &&
                savedPet.breed == "Golden Retriever" &&
                savedPet.age == 3 &&
                savedPet.birthdate == null &&
                savedPet.weight == null &&
                savedPet.nickname == null &&
                savedPet.owner == "John Doe" &&
                savedPet.photoUrl == null &&
                savedPet.registrationDate == savedPetData.registrationDate
            }
            .verifyComplete()

        val petDataCaptor = argumentCaptor<PetData>()
        verify(petRepository).save(petDataCaptor.capture())

        val capturedPetData = petDataCaptor.firstValue
        assertEquals("Buddy", capturedPetData.name)
        assertEquals("DOG", capturedPetData.species)
        assertEquals("Golden Retriever", capturedPetData.breed)
        assertEquals(3, capturedPetData.age)
        assertEquals("John Doe", capturedPetData.owner)
        assertNull(capturedPetData.id) // Should be null for new entity
    }

    @Test
    fun `save should handle pet with existing ID`() {
        // Given
        val existingId = UUID.randomUUID()
        val petModel = Pet(
            id = existingId.toString(),
            name = "Buddy",
            species = Species.DOG,
            breed = "Golden Retriever",
            age = 3,
            owner = "John Doe",
            registrationDate = LocalDate.now()
        )

        val savedPetData = PetData(
            id = existingId,
            name = "Buddy",
            species = "DOG",
            breed = "Golden Retriever",
            age = 3,
            owner = "John Doe",
            registrationDate = LocalDate.now()
        )

        `when`(petRepository.save(any<PetData>())).thenReturn(Mono.just(savedPetData))

        // When
        val result = petPersistenceAdapter.save(petModel)

        // Then
        StepVerifier.create(result)
            .expectNextMatches { savedPet ->
                savedPet.id == existingId.toString()
            }
            .verifyComplete()

        val petDataCaptor = argumentCaptor<PetData>()
        verify(petRepository).save(petDataCaptor.capture())
        
        val capturedPetData = petDataCaptor.firstValue
        assertEquals(existingId, capturedPetData.id)
    }

    @Test
    fun `save should handle pet with null breed`() {
        // Given
        val petModel = Pet(
            id = null,
            name = "Mittens",
            species = Species.CAT,
            breed = null,
            age = 2,
            owner = "Jane Smith",
            registrationDate = LocalDate.now()
        )

        val savedPetData = PetData(
            id = UUID.randomUUID(),
            name = "Mittens",
            species = "CAT",
            breed = null,
            age = 2,
            owner = "Jane Smith",
            registrationDate = LocalDate.now()
        )

        `when`(petRepository.save(any<PetData>())).thenReturn(Mono.just(savedPetData))

        // When
        val result = petPersistenceAdapter.save(petModel)

        // Then
        StepVerifier.create(result)
            .expectNextMatches { savedPet ->
                savedPet.breed == null
            }
            .verifyComplete()

        val petDataCaptor = argumentCaptor<PetData>()
        verify(petRepository).save(petDataCaptor.capture())
        
        val capturedPetData = petDataCaptor.firstValue
        assertNull(capturedPetData.breed)
    }

    @Test
    fun `save should propagate repository errors`() {
        // Given
        val petModel = Pet(
            id = null,
            name = "Buddy",
            species = Species.DOG,
            breed = "Golden Retriever",
            age = 3,
            owner = "John Doe",
            registrationDate = LocalDate.now()
        )

        val repositoryError = RuntimeException("Database constraint violation")
        `when`(petRepository.save(any<PetData>())).thenReturn(Mono.error(repositoryError))

        // When
        val result = petPersistenceAdapter.save(petModel)

        // Then
        StepVerifier.create(result)
            .expectErrorMatches { throwable ->
                throwable is RuntimeException && throwable.message == "Database constraint violation"
            }
            .verify()

        verify(petRepository).save(any())
    }

    @Test
    fun `save should handle empty string values`() {
        // Given
        val petModel = Pet(
            id = null,
            name = "Rex",
            species = Species.DOG,
            breed = "", // Empty string
            age = 1,
            owner = "Bob Wilson",
            registrationDate = LocalDate.now()
        )

        val savedPetData = PetData(
            id = UUID.randomUUID(),
            name = "Rex",
            species = "DOG",
            breed = "",
            age = 1,
            owner = "Bob Wilson",
            registrationDate = LocalDate.now()
        )

        `when`(petRepository.save(any<PetData>())).thenReturn(Mono.just(savedPetData))

        // When
        val result = petPersistenceAdapter.save(petModel)

        // Then
        StepVerifier.create(result)
            .expectNextMatches { savedPet ->
                savedPet.breed == ""
            }
            .verifyComplete()

        val petDataCaptor = argumentCaptor<PetData>()
        verify(petRepository).save(petDataCaptor.capture())
        
        val capturedPetData = petDataCaptor.firstValue
        assertEquals("", capturedPetData.breed)
    }

    @Test
    fun `save should preserve all date information`() {
        // Given
        val specificDate = LocalDate.of(2023, 12, 25)
        val petModel = Pet(
            id = null,
            name = "Christmas",
            species = Species.CAT,
            breed = "Persian",
            age = 1,
            birthdate = null,
            weight = null,
            nickname = null,
            owner = "Holiday Family",
            registrationDate = specificDate,
            photoUrl = null
        )

        val savedPetData = PetData(
            id = UUID.randomUUID(),
            name = "Christmas",
            species = "CAT",
            breed = "Persian",
            age = 1,
            birthdate = null,
            weight = null,
            nickname = null,
            owner = "Holiday Family",
            registrationDate = specificDate,
            photoUrl = null
        )

        `when`(petRepository.save(any<PetData>())).thenReturn(Mono.just(savedPetData))

        // When
        val result = petPersistenceAdapter.save(petModel)

        // Then
        StepVerifier.create(result)
            .expectNextMatches { savedPet ->
                savedPet.registrationDate == specificDate
            }
            .verifyComplete()

        val petDataCaptor = argumentCaptor<PetData>()
        verify(petRepository).save(petDataCaptor.capture())

        val capturedPetData = petDataCaptor.firstValue
        assertEquals(specificDate, capturedPetData.registrationDate)
    }

    @Test
    fun `countByOwner should delegate to repository and return count`() {
        // Given
        val userId = "user-123"
        val expectedCount = 5L

        `when`(petRepository.countByOwner(userId)).thenReturn(Mono.just(expectedCount))

        // When
        val result = petPersistenceAdapter.countByOwner(userId)

        // Then
        StepVerifier.create(result)
            .expectNext(expectedCount)
            .verifyComplete()

        verify(petRepository).countByOwner(userId)
    }

    @Test
    fun `countByOwner should return zero when user has no pets`() {
        // Given
        val userId = "user-new"

        `when`(petRepository.countByOwner(userId)).thenReturn(Mono.just(0L))

        // When
        val result = petPersistenceAdapter.countByOwner(userId)

        // Then
        StepVerifier.create(result)
            .expectNext(0L)
            .verifyComplete()

        verify(petRepository).countByOwner(userId)
    }

    @Test
    fun `countByOwner should propagate repository errors`() {
        // Given
        val userId = "user-123"
        val repositoryError = RuntimeException("Database connection failed")

        `when`(petRepository.countByOwner(userId)).thenReturn(Mono.error(repositoryError))

        // When
        val result = petPersistenceAdapter.countByOwner(userId)

        // Then
        StepVerifier.create(result)
            .expectErrorMatches { throwable ->
                throwable is RuntimeException && throwable.message == "Database connection failed"
            }
            .verify()

        verify(petRepository).countByOwner(userId)
    }

    @Test
    fun `findById should return pet when found`() {
        // Given
        val petId = UUID.randomUUID()
        val petData = PetData(
            id = petId,
            name = "Buddy",
            species = "DOG",
            breed = "Golden Retriever",
            age = 3,
            birthdate = null,
            weight = null,
            nickname = null,
            owner = "user-123",
            registrationDate = LocalDate.now(),
            photoUrl = null
        )

        `when`(petRepository.findById(petId)).thenReturn(Mono.just(petData))

        // When
        val result = petPersistenceAdapter.findById(petId.toString())

        // Then
        StepVerifier.create(result)
            .expectNextMatches { pet ->
                pet.id == petId.toString() &&
                pet.name == "Buddy" &&
                pet.species == Species.DOG &&
                pet.breed == "Golden Retriever" &&
                pet.age == 3 &&
                pet.owner == "user-123"
            }
            .verifyComplete()

        verify(petRepository).findById(petId)
    }

    @Test
    fun `findById should return empty when not found`() {
        // Given
        val petId = UUID.randomUUID()

        `when`(petRepository.findById(petId)).thenReturn(Mono.empty())

        // When
        val result = petPersistenceAdapter.findById(petId.toString())

        // Then
        StepVerifier.create(result)
            .verifyComplete()

        verify(petRepository).findById(petId)
    }

    @Test
    fun `findById should propagate repository errors`() {
        // Given
        val petId = UUID.randomUUID()
        val repositoryError = RuntimeException("Database query failed")

        `when`(petRepository.findById(petId)).thenReturn(Mono.error(repositoryError))

        // When
        val result = petPersistenceAdapter.findById(petId.toString())

        // Then
        StepVerifier.create(result)
            .expectErrorMatches { throwable ->
                throwable is RuntimeException && throwable.message == "Database query failed"
            }
            .verify()

        verify(petRepository).findById(petId)
    }
}