package com.donperry.persistence.pet.mapper

import com.donperry.model.pet.Pet
import com.donperry.persistence.pet.entities.PetData
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertNull

class PetMapperTest {

    @Test
    fun `toEntity should convert Pet model to PetData entity with all fields`() {
        // Given
        val petId = UUID.randomUUID()
        val registrationDate = LocalDate.of(2023, 12, 25)
        
        val petModel = Pet(
            id = petId.toString(),
            name = "Buddy",
            species = "Dog",
            breed = "Golden Retriever",
            age = 3,
            owner = "John Doe",
            registrationDate = registrationDate
        )

        // When
        val result = PetMapper.toEntity(petModel)

        // Then
        assertEquals(petId, result.id)
        assertEquals("Buddy", result.name)
        assertEquals("Dog", result.species)
        assertEquals("Golden Retriever", result.breed)
        assertEquals(3, result.age)
        assertEquals("John Doe", result.owner)
        assertEquals(registrationDate, result.registrationDate)
    }

    @Test
    fun `toEntity should handle Pet model with null id`() {
        // Given
        val petModel = Pet(
            id = null,
            name = "Mittens",
            species = "Cat",
            breed = "Persian",
            age = 2,
            owner = "Jane Smith",
            registrationDate = LocalDate.now()
        )

        // When
        val result = PetMapper.toEntity(petModel)

        // Then
        assertNull(result.id)
        assertEquals("Mittens", result.name)
        assertEquals("Cat", result.species)
        assertEquals("Persian", result.breed)
        assertEquals(2, result.age)
        assertEquals("Jane Smith", result.owner)
    }

    @Test
    fun `toEntity should handle Pet model with null breed`() {
        // Given
        val petModel = Pet(
            id = UUID.randomUUID().toString(),
            name = "Rex",
            species = "Dog",
            breed = null,
            age = 5,
            owner = "Bob Wilson",
            registrationDate = LocalDate.now()
        )

        // When
        val result = PetMapper.toEntity(petModel)

        // Then
        assertNull(result.breed)
        assertEquals("Rex", result.name)
        assertEquals("Dog", result.species)
        assertEquals(5, result.age)
        assertEquals("Bob Wilson", result.owner)
    }

    @Test
    fun `toEntity should handle Pet model with empty string breed`() {
        // Given
        val petModel = Pet(
            id = UUID.randomUUID().toString(),
            name = "Fluffy",
            species = "Cat",
            breed = "",
            age = 1,
            owner = "Alice Brown",
            registrationDate = LocalDate.now()
        )

        // When
        val result = PetMapper.toEntity(petModel)

        // Then
        assertEquals("", result.breed)
        assertEquals("Fluffy", result.name)
    }

    @Test
    fun `toModel should convert PetData entity to Pet model with all fields`() {
        // Given
        val petId = UUID.randomUUID()
        val registrationDate = LocalDate.of(2023, 11, 15)
        
        val petData = PetData(
            id = petId,
            name = "Charlie",
            species = "Dog",
            breed = "Labrador",
            age = 4,
            owner = "Mike Johnson",
            registrationDate = registrationDate
        )

        // When
        val result = PetMapper.toModel(petData)

        // Then
        assertEquals(petId.toString(), result.id)
        assertEquals("Charlie", result.name)
        assertEquals("Dog", result.species)
        assertEquals("Labrador", result.breed)
        assertEquals(4, result.age)
        assertEquals("Mike Johnson", result.owner)
        assertEquals(registrationDate, result.registrationDate)
    }

    @Test
    fun `toModel should handle PetData entity with null breed`() {
        // Given
        val petId = UUID.randomUUID()
        val petData = PetData(
            id = petId,
            name = "Whiskers",
            species = "Cat",
            breed = null,
            age = 3,
            owner = "Sarah Connor",
            registrationDate = LocalDate.now()
        )

        // When
        val result = PetMapper.toModel(petData)

        // Then
        assertEquals(petId.toString(), result.id)
        assertNull(result.breed)
        assertEquals("Whiskers", result.name)
        assertEquals("Cat", result.species)
    }

    @Test
    fun `toModel should handle PetData entity with empty string breed`() {
        // Given
        val petId = UUID.randomUUID()
        val petData = PetData(
            id = petId,
            name = "Spot",
            species = "Dog",
            breed = "",
            age = 2,
            owner = "Tom Anderson",
            registrationDate = LocalDate.now()
        )

        // When
        val result = PetMapper.toModel(petData)

        // Then
        assertEquals("", result.breed)
        assertEquals("Spot", result.name)
    }

    @Test
    fun `toModel and toEntity should be bidirectional for complete Pet`() {
        // Given
        val originalPet = Pet(
            id = UUID.randomUUID().toString(),
            name = "Bidirectional",
            species = "Dog",
            breed = "Mixed",
            age = 6,
            owner = "Test Owner",
            registrationDate = LocalDate.of(2023, 10, 10)
        )

        // When
        val petData = PetMapper.toEntity(originalPet)
        val convertedPet = PetMapper.toModel(petData)

        // Then
        assertEquals(originalPet.id, convertedPet.id)
        assertEquals(originalPet.name, convertedPet.name)
        assertEquals(originalPet.species, convertedPet.species)
        assertEquals(originalPet.breed, convertedPet.breed)
        assertEquals(originalPet.age, convertedPet.age)
        assertEquals(originalPet.owner, convertedPet.owner)
        assertEquals(originalPet.registrationDate, convertedPet.registrationDate)
    }

    @Test
    fun `toModel and toEntity should be bidirectional for Pet with null values`() {
        // Given
        val originalPet = Pet(
            id = null,
            name = "NullTest",
            species = "Cat",
            breed = null,
            age = 1,
            owner = "Null Owner",
            registrationDate = LocalDate.now()
        )

        // When
        val petData = PetMapper.toEntity(originalPet)
        val convertedPetData = petData.copy(id = UUID.randomUUID()) // Simulate DB assigning ID
        val convertedPet = PetMapper.toModel(convertedPetData)

        // Then
        assertEquals(convertedPetData.id.toString(), convertedPet.id)
        assertEquals(originalPet.name, convertedPet.name)
        assertEquals(originalPet.species, convertedPet.species)
        assertNull(convertedPet.breed)
        assertEquals(originalPet.age, convertedPet.age)
        assertEquals(originalPet.owner, convertedPet.owner)
        assertEquals(originalPet.registrationDate, convertedPet.registrationDate)
    }

    @Test
    fun `toEntity should handle zero age correctly`() {
        // Given
        val petModel = Pet(
            id = UUID.randomUUID().toString(),
            name = "NewBorn",
            species = "Cat",
            breed = "Siamese",
            age = 0,
            owner = "First Time Owner",
            registrationDate = LocalDate.now()
        )

        // When
        val result = PetMapper.toEntity(petModel)

        // Then
        assertEquals(0, result.age)
    }

    @Test
    fun `toModel should handle zero age correctly`() {
        // Given
        val petData = PetData(
            id = UUID.randomUUID(),
            name = "Baby",
            species = "Dog",
            breed = "Puppy",
            age = 0,
            owner = "New Parent",
            registrationDate = LocalDate.now()
        )

        // When
        val result = PetMapper.toModel(petData)

        // Then
        assertEquals(0, result.age)
    }
}