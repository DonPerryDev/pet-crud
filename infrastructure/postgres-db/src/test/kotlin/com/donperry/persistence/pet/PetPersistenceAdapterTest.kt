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
import java.math.BigDecimal
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

        val result = petPersistenceAdapter.save(petModel)

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

        val result = petPersistenceAdapter.save(petModel)

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

        val result = petPersistenceAdapter.save(petModel)

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

        val result = petPersistenceAdapter.save(petModel)

        StepVerifier.create(result)
            .expectErrorMatches { throwable ->
                throwable is RuntimeException && throwable.message == "Database constraint violation"
            }
            .verify()

        verify(petRepository).save(any())
    }

    @Test
    fun `save should handle empty string values`() {
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

        val result = petPersistenceAdapter.save(petModel)

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

        val result = petPersistenceAdapter.save(petModel)

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
        val userId = "user-123"
        val expectedCount = 5L

        `when`(petRepository.countByOwner(userId)).thenReturn(Mono.just(expectedCount))

        val result = petPersistenceAdapter.countByOwner(userId)

        StepVerifier.create(result)
            .expectNext(expectedCount)
            .verifyComplete()

        verify(petRepository).countByOwner(userId)
    }

    @Test
    fun `countByOwner should return zero when user has no pets`() {
        val userId = "user-new"

        `when`(petRepository.countByOwner(userId)).thenReturn(Mono.just(0L))

        val result = petPersistenceAdapter.countByOwner(userId)

        StepVerifier.create(result)
            .expectNext(0L)
            .verifyComplete()

        verify(petRepository).countByOwner(userId)
    }

    @Test
    fun `countByOwner should propagate repository errors`() {
        val userId = "user-123"
        val repositoryError = RuntimeException("Database connection failed")

        `when`(petRepository.countByOwner(userId)).thenReturn(Mono.error(repositoryError))

        val result = petPersistenceAdapter.countByOwner(userId)

        StepVerifier.create(result)
            .expectErrorMatches { throwable ->
                throwable is RuntimeException && throwable.message == "Database connection failed"
            }
            .verify()

        verify(petRepository).countByOwner(userId)
    }

    @Test
    fun `findById should return pet when found`() {
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

        val result = petPersistenceAdapter.findById(petId.toString())

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
        val petId = UUID.randomUUID()

        `when`(petRepository.findById(petId)).thenReturn(Mono.empty())

        val result = petPersistenceAdapter.findById(petId.toString())

        StepVerifier.create(result)
            .verifyComplete()

        verify(petRepository).findById(petId)
    }

    @Test
    fun `findById should propagate repository errors`() {
        val petId = UUID.randomUUID()
        val repositoryError = RuntimeException("Database query failed")

        `when`(petRepository.findById(petId)).thenReturn(Mono.error(repositoryError))

        val result = petPersistenceAdapter.findById(petId.toString())

        StepVerifier.create(result)
            .expectErrorMatches { throwable ->
                throwable is RuntimeException && throwable.message == "Database query failed"
            }
            .verify()

        verify(petRepository).findById(petId)
    }

    @Test
    fun `update should convert model to entity, save and convert back to model`() {
        val petId = UUID.randomUUID()
        val petModel = Pet(
            id = petId.toString(),
            name = "Buddy Updated",
            species = Species.CAT,
            breed = "Persian",
            age = 4,
            birthdate = LocalDate.of(2020, 5, 10),
            weight = BigDecimal("30.0"),
            nickname = "Buddy Bear",
            owner = "user-123",
            registrationDate = LocalDate.of(2023, 6, 1),
            photoUrl = "https://example.com/photo.jpg"
        )

        val updatedPetData = PetData(
            id = petId,
            name = "Buddy Updated",
            species = "CAT",
            breed = "Persian",
            age = 4,
            birthdate = LocalDate.of(2020, 5, 10),
            weight = BigDecimal("30.0"),
            nickname = "Buddy Bear",
            owner = "user-123",
            registrationDate = LocalDate.of(2023, 6, 1),
            photoUrl = "https://example.com/photo.jpg"
        )

        `when`(petRepository.save(any<PetData>())).thenReturn(Mono.just(updatedPetData))

        val result = petPersistenceAdapter.update(petModel)

        StepVerifier.create(result)
            .expectNextMatches { updatedPet ->
                updatedPet.id == petId.toString() &&
                updatedPet.name == "Buddy Updated" &&
                updatedPet.species == Species.CAT &&
                updatedPet.breed == "Persian" &&
                updatedPet.age == 4 &&
                updatedPet.birthdate == LocalDate.of(2020, 5, 10) &&
                updatedPet.weight == BigDecimal("30.0") &&
                updatedPet.nickname == "Buddy Bear" &&
                updatedPet.owner == "user-123" &&
                updatedPet.registrationDate == LocalDate.of(2023, 6, 1) &&
                updatedPet.photoUrl == "https://example.com/photo.jpg"
            }
            .verifyComplete()

        val petDataCaptor = argumentCaptor<PetData>()
        verify(petRepository).save(petDataCaptor.capture())

        val capturedPetData = petDataCaptor.firstValue
        assertEquals("Buddy Updated", capturedPetData.name)
        assertEquals("CAT", capturedPetData.species)
        assertEquals("Persian", capturedPetData.breed)
        assertEquals(4, capturedPetData.age)
        assertEquals(LocalDate.of(2020, 5, 10), capturedPetData.birthdate)
        assertEquals(BigDecimal("30.0"), capturedPetData.weight)
        assertEquals("Buddy Bear", capturedPetData.nickname)
        assertEquals("user-123", capturedPetData.owner)
        assertEquals(LocalDate.of(2023, 6, 1), capturedPetData.registrationDate)
        assertEquals("https://example.com/photo.jpg", capturedPetData.photoUrl)
        assertEquals(petId, capturedPetData.id)
    }

    @Test
    fun `update should handle pet with null optional fields`() {
        val petId = UUID.randomUUID()
        val petModel = Pet(
            id = petId.toString(),
            name = "Buddy",
            species = Species.DOG,
            breed = null,
            age = 3,
            birthdate = null,
            weight = null,
            nickname = null,
            owner = "user-123",
            registrationDate = LocalDate.now(),
            photoUrl = null
        )

        val updatedPetData = PetData(
            id = petId,
            name = "Buddy",
            species = "DOG",
            breed = null,
            age = 3,
            birthdate = null,
            weight = null,
            nickname = null,
            owner = "user-123",
            registrationDate = LocalDate.now(),
            photoUrl = null
        )

        `when`(petRepository.save(any<PetData>())).thenReturn(Mono.just(updatedPetData))

        val result = petPersistenceAdapter.update(petModel)

        StepVerifier.create(result)
            .expectNextMatches { updatedPet ->
                updatedPet.breed == null &&
                updatedPet.birthdate == null &&
                updatedPet.weight == null &&
                updatedPet.nickname == null &&
                updatedPet.photoUrl == null
            }
            .verifyComplete()

        val petDataCaptor = argumentCaptor<PetData>()
        verify(petRepository).save(petDataCaptor.capture())

        val capturedPetData = petDataCaptor.firstValue
        assertNull(capturedPetData.breed)
        assertNull(capturedPetData.birthdate)
        assertNull(capturedPetData.weight)
        assertNull(capturedPetData.nickname)
        assertNull(capturedPetData.photoUrl)
    }

    @Test
    fun `update should propagate repository errors`() {
        val petId = UUID.randomUUID()
        val petModel = Pet(
            id = petId.toString(),
            name = "Buddy",
            species = Species.DOG,
            breed = "Golden Retriever",
            age = 3,
            owner = "user-123",
            registrationDate = LocalDate.now()
        )

        val repositoryError = RuntimeException("Database update failed")
        `when`(petRepository.save(any<PetData>())).thenReturn(Mono.error(repositoryError))

        val result = petPersistenceAdapter.update(petModel)

        StepVerifier.create(result)
            .expectErrorMatches { throwable ->
                throwable is RuntimeException && throwable.message == "Database update failed"
            }
            .verify()

        verify(petRepository).save(any())
    }

    @Test
    fun `update should preserve id and registrationDate`() {
        val petId = UUID.randomUUID()
        val originalRegistrationDate = LocalDate.of(2023, 6, 1)
        val petModel = Pet(
            id = petId.toString(),
            name = "Buddy Updated",
            species = Species.DOG,
            breed = "Golden Retriever",
            age = 4,
            owner = "user-123",
            registrationDate = originalRegistrationDate
        )

        val updatedPetData = PetData(
            id = petId,
            name = "Buddy Updated",
            species = "DOG",
            breed = "Golden Retriever",
            age = 4,
            owner = "user-123",
            registrationDate = originalRegistrationDate
        )

        `when`(petRepository.save(any<PetData>())).thenReturn(Mono.just(updatedPetData))

        val result = petPersistenceAdapter.update(petModel)

        StepVerifier.create(result)
            .expectNextMatches { updatedPet ->
                updatedPet.id == petId.toString() &&
                updatedPet.registrationDate == originalRegistrationDate
            }
            .verifyComplete()

        val petDataCaptor = argumentCaptor<PetData>()
        verify(petRepository).save(petDataCaptor.capture())

        val capturedPetData = petDataCaptor.firstValue
        assertEquals(petId, capturedPetData.id)
        assertEquals(originalRegistrationDate, capturedPetData.registrationDate)
    }
}