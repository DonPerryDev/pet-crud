package com.donperry.persistence.pet

import com.donperry.model.pet.Pet
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
            species = "Dog",
            breed = "Golden Retriever",
            age = 3,
            owner = "John Doe",
            registrationDate = LocalDate.now()
        )

        val savedPetData = PetData(
            id = UUID.randomUUID(),
            name = "Buddy",
            species = "Dog",
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
                savedPet.id == savedPetData.id.toString() &&
                savedPet.name == "Buddy" &&
                savedPet.species == "Dog" &&
                savedPet.breed == "Golden Retriever" &&
                savedPet.age == 3 &&
                savedPet.owner == "John Doe" &&
                savedPet.registrationDate == savedPetData.registrationDate
            }
            .verifyComplete()

        val petDataCaptor = argumentCaptor<PetData>()
        verify(petRepository).save(petDataCaptor.capture())
        
        val capturedPetData = petDataCaptor.firstValue
        assertEquals("Buddy", capturedPetData.name)
        assertEquals("Dog", capturedPetData.species)
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
            species = "Dog",
            breed = "Golden Retriever",
            age = 3,
            owner = "John Doe",
            registrationDate = LocalDate.now()
        )

        val savedPetData = PetData(
            id = existingId,
            name = "Buddy",
            species = "Dog",
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
            species = "Cat",
            breed = null,
            age = 2,
            owner = "Jane Smith",
            registrationDate = LocalDate.now()
        )

        val savedPetData = PetData(
            id = UUID.randomUUID(),
            name = "Mittens",
            species = "Cat",
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
            species = "Dog",
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
            species = "Dog",
            breed = "", // Empty string
            age = 1,
            owner = "Bob Wilson",
            registrationDate = LocalDate.now()
        )

        val savedPetData = PetData(
            id = UUID.randomUUID(),
            name = "Rex",
            species = "Dog",
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
            species = "Cat",
            breed = "Persian",
            age = 1,
            owner = "Holiday Family",
            registrationDate = specificDate
        )

        val savedPetData = PetData(
            id = UUID.randomUUID(),
            name = "Christmas",
            species = "Cat",
            breed = "Persian",
            age = 1,
            owner = "Holiday Family",
            registrationDate = specificDate
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
}