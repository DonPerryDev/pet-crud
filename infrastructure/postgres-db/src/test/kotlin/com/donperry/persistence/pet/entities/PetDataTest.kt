package com.donperry.persistence.pet.entities

import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertNull

class PetDataTest {

    @Test
    fun `PetData should be created with all required fields`() {
        // Given
        val id = UUID.randomUUID()
        val name = "Buddy"
        val species = "Dog"
        val breed = "Golden Retriever"
        val age = 3
        val owner = "John Doe"
        val registrationDate = LocalDate.of(2023, 12, 25)

        // When
        val petData = PetData(
            id = id,
            name = name,
            species = species,
            breed = breed,
            age = age,
            owner = owner,
            registrationDate = registrationDate
        )

        // Then
        assertEquals(id, petData.id)
        assertEquals(name, petData.name)
        assertEquals(species, petData.species)
        assertEquals(breed, petData.breed)
        assertEquals(age, petData.age)
        assertEquals(owner, petData.owner)
        assertEquals(registrationDate, petData.registrationDate)
    }

    @Test
    fun `PetData should be created with null id`() {
        // When
        val petData = PetData(
            id = null,
            name = "Mittens",
            species = "Cat",
            breed = "Persian",
            age = 2,
            owner = "Jane Smith",
            registrationDate = LocalDate.now()
        )

        // Then
        assertNull(petData.id)
        assertEquals("Mittens", petData.name)
        assertEquals("Cat", petData.species)
        assertEquals("Persian", petData.breed)
        assertEquals(2, petData.age)
        assertEquals("Jane Smith", petData.owner)
    }

    @Test
    fun `PetData should handle null breed`() {
        // When
        val petData = PetData(
            id = UUID.randomUUID(),
            name = "Rex",
            species = "Dog",
            breed = null,
            age = 5,
            owner = "Bob Wilson",
            registrationDate = LocalDate.now()
        )

        // Then
        assertNull(petData.breed)
        assertEquals("Rex", petData.name)
        assertEquals("Dog", petData.species)
        assertEquals(5, petData.age)
        assertEquals("Bob Wilson", petData.owner)
    }

    @Test
    fun `PetData should handle empty string breed`() {
        // When
        val petData = PetData(
            id = UUID.randomUUID(),
            name = "Fluffy",
            species = "Cat",
            breed = "",
            age = 1,
            owner = "Alice Brown",
            registrationDate = LocalDate.now()
        )

        // Then
        assertEquals("", petData.breed)
        assertEquals("Fluffy", petData.name)
        assertEquals("Cat", petData.species)
        assertEquals(1, petData.age)
        assertEquals("Alice Brown", petData.owner)
    }

    @Test
    fun `PetData should handle zero age`() {
        // When
        val petData = PetData(
            id = UUID.randomUUID(),
            name = "NewBorn",
            species = "Hamster",
            breed = "Syrian",
            age = 0,
            owner = "Pet Store",
            registrationDate = LocalDate.now()
        )

        // Then
        assertEquals(0, petData.age)
        assertEquals("NewBorn", petData.name)
        assertEquals("Hamster", petData.species)
        assertEquals("Syrian", petData.breed)
        assertEquals("Pet Store", petData.owner)
    }

    @Test
    fun `PetData equality should work correctly with same data`() {
        // Given
        val id = UUID.randomUUID()
        val registrationDate = LocalDate.of(2023, 10, 15)
        
        val petData1 = PetData(
            id = id,
            name = "Buddy",
            species = "Dog",
            breed = "Golden Retriever",
            age = 3,
            owner = "John Doe",
            registrationDate = registrationDate
        )
        
        val petData2 = PetData(
            id = id,
            name = "Buddy",
            species = "Dog",
            breed = "Golden Retriever",
            age = 3,
            owner = "John Doe",
            registrationDate = registrationDate
        )

        // Then
        assertEquals(petData1, petData2)
        assertEquals(petData1.hashCode(), petData2.hashCode())
    }

    @Test
    fun `PetData equality should work correctly with different data`() {
        // Given
        val petData1 = PetData(
            id = UUID.randomUUID(),
            name = "Buddy",
            species = "Dog",
            breed = "Golden Retriever",
            age = 3,
            owner = "John Doe",
            registrationDate = LocalDate.of(2023, 10, 15)
        )
        
        val petData2 = PetData(
            id = UUID.randomUUID(),
            name = "Mittens",
            species = "Cat",
            breed = "Persian",
            age = 2,
            owner = "Jane Smith",
            registrationDate = LocalDate.of(2023, 11, 20)
        )

        // Then
        assertNotEquals(petData1, petData2)
        assertNotEquals(petData1.hashCode(), petData2.hashCode())
    }

    @Test
    fun `PetData equality should work correctly with different ids`() {
        // Given
        val registrationDate = LocalDate.now()
        
        val petData1 = PetData(
            id = UUID.randomUUID(),
            name = "Buddy",
            species = "Dog",
            breed = "Golden Retriever",
            age = 3,
            owner = "John Doe",
            registrationDate = registrationDate
        )
        
        val petData2 = PetData(
            id = UUID.randomUUID(),
            name = "Buddy",
            species = "Dog",
            breed = "Golden Retriever",
            age = 3,
            owner = "John Doe",
            registrationDate = registrationDate
        )

        // Then
        assertNotEquals(petData1, petData2)
    }

    @Test
    fun `PetData should support component destructuring`() {
        // Given
        val id = UUID.randomUUID()
        val registrationDate = LocalDate.of(2023, 11, 20)
        
        val petData = PetData(
            id = id,
            name = "Charlie",
            species = "Dog",
            breed = "Labrador",
            age = 4,
            owner = "Mike Johnson",
            registrationDate = registrationDate
        )

        // When
        val (dataId, name, species, breed, age, owner, dataRegistrationDate) = petData

        // Then
        assertEquals(id, dataId)
        assertEquals("Charlie", name)
        assertEquals("Dog", species)
        assertEquals("Labrador", breed)
        assertEquals(4, age)
        assertEquals("Mike Johnson", owner)
        assertEquals(registrationDate, dataRegistrationDate)
    }

    @Test
    fun `PetData copy should work correctly`() {
        // Given
        val originalPetData = PetData(
            id = UUID.randomUUID(),
            name = "Buddy",
            species = "Dog",
            breed = "Golden Retriever",
            age = 3,
            owner = "John Doe",
            registrationDate = LocalDate.now()
        )

        // When
        val copiedPetData = originalPetData.copy(age = 4, owner = "Jane Doe")

        // Then
        assertEquals(originalPetData.id, copiedPetData.id)
        assertEquals(originalPetData.name, copiedPetData.name)
        assertEquals(originalPetData.species, copiedPetData.species)
        assertEquals(originalPetData.breed, copiedPetData.breed)
        assertEquals(4, copiedPetData.age) // Changed
        assertEquals("Jane Doe", copiedPetData.owner) // Changed
        assertEquals(originalPetData.registrationDate, copiedPetData.registrationDate)
    }

    @Test
    fun `PetData toString should include all fields`() {
        // Given
        val id = UUID.fromString("550e8400-e29b-41d4-a716-446655440000")
        val petData = PetData(
            id = id,
            name = "Buddy",
            species = "Dog",
            breed = "Golden Retriever",
            age = 3,
            owner = "John Doe",
            registrationDate = LocalDate.of(2023, 12, 25)
        )

        // When
        val toStringResult = petData.toString()

        // Then
        assert(toStringResult.contains("550e8400-e29b-41d4-a716-446655440000"))
        assert(toStringResult.contains("Buddy"))
        assert(toStringResult.contains("Dog"))
        assert(toStringResult.contains("Golden Retriever"))
        assert(toStringResult.contains("3"))
        assert(toStringResult.contains("John Doe"))
        assert(toStringResult.contains("2023-12-25"))
    }

    @Test
    fun `PetData should handle special characters in strings`() {
        // When
        val petData = PetData(
            id = UUID.randomUUID(),
            name = "Señor Fluffington",
            species = "Cat",
            breed = "Maine Coon",
            age = 7,
            owner = "María José González",
            registrationDate = LocalDate.now()
        )

        // Then
        assertEquals("Señor Fluffington", petData.name)
        assertEquals("Cat", petData.species)
        assertEquals("Maine Coon", petData.breed)
        assertEquals(7, petData.age)
        assertEquals("María José González", petData.owner)
    }

    @Test
    fun `PetData should preserve UUID format`() {
        // Given
        val specificUUID = UUID.fromString("123e4567-e89b-12d3-a456-426614174000")

        // When
        val petData = PetData(
            id = specificUUID,
            name = "UUID Test Pet",
            species = "Digital",
            breed = "Virtual",
            age = 1,
            owner = "Test Owner",
            registrationDate = LocalDate.now()
        )

        // Then
        assertEquals(specificUUID, petData.id)
        assertEquals("123e4567-e89b-12d3-a456-426614174000", petData.id.toString())
    }

    @Test
    fun `PetData should handle different date ranges`() {
        // Given
        val veryOldDate = LocalDate.of(1900, 1, 1)
        val futureDate = LocalDate.of(2100, 12, 31)

        // When
        val oldPetData = PetData(
            id = UUID.randomUUID(),
            name = "Ancient Pet",
            species = "Dinosaur",
            breed = "T-Rex",
            age = 999,
            owner = "Paleontologist",
            registrationDate = veryOldDate
        )

        val futurePetData = PetData(
            id = UUID.randomUUID(),
            name = "Future Pet",
            species = "Robot",
            breed = "AI",
            age = 1,
            owner = "Futurist",
            registrationDate = futureDate
        )

        // Then
        assertEquals(veryOldDate, oldPetData.registrationDate)
        assertEquals(futureDate, futurePetData.registrationDate)
    }
}