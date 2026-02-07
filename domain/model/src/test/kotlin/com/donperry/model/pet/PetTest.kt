package com.donperry.model.pet

import org.junit.jupiter.api.Test
import java.time.LocalDate
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertNull

class PetTest {

    @Test
    fun `Pet should be created with all required fields`() {
        // Given
        val name = "Buddy"
        val species = "Dog"
        val breed = "Golden Retriever"
        val age = 3
        val owner = "John Doe"
        val registrationDate = LocalDate.of(2023, 12, 25)

        // When
        val pet = Pet(
            id = "pet-123",
            name = name,
            species = species,
            breed = breed,
            age = age,
            owner = owner,
            registrationDate = registrationDate
        )

        // Then
        assertEquals("pet-123", pet.id)
        assertEquals(name, pet.name)
        assertEquals(species, pet.species)
        assertEquals(breed, pet.breed)
        assertEquals(age, pet.age)
        assertEquals(owner, pet.owner)
        assertEquals(registrationDate, pet.registrationDate)
    }

    @Test
    fun `Pet should be created with null id`() {
        // When
        val pet = Pet(
            id = null,
            name = "Mittens",
            species = "Cat",
            breed = "Persian",
            age = 2,
            owner = "Jane Smith",
            registrationDate = LocalDate.now()
        )

        // Then
        assertNull(pet.id)
        assertEquals("Mittens", pet.name)
    }

    @Test
    fun `Pet should be created with null breed`() {
        // When
        val pet = Pet(
            id = "pet-456",
            name = "Rex",
            species = "Dog",
            breed = null,
            age = 5,
            owner = "Bob Wilson",
            registrationDate = LocalDate.now()
        )

        // Then
        assertNull(pet.breed)
        assertEquals("Rex", pet.name)
        assertEquals("Dog", pet.species)
    }

    @Test
    fun `Pet should handle empty string breed`() {
        // When
        val pet = Pet(
            id = "pet-789",
            name = "Fluffy",
            species = "Cat",
            breed = "",
            age = 1,
            owner = "Alice Brown",
            registrationDate = LocalDate.now()
        )

        // Then
        assertEquals("", pet.breed)
        assertEquals("Fluffy", pet.name)
    }

    @Test
    fun `Pet should handle zero age`() {
        // When
        val pet = Pet(
            id = "pet-000",
            name = "NewBorn",
            species = "Hamster",
            breed = "Syrian",
            age = 0,
            owner = "Pet Store",
            registrationDate = LocalDate.now()
        )

        // Then
        assertEquals(0, pet.age)
        assertEquals("NewBorn", pet.name)
    }

    @Test
    fun `Pet equality should work correctly with same data`() {
        // Given
        val registrationDate = LocalDate.of(2023, 10, 15)
        val pet1 = Pet(
            id = "pet-123",
            name = "Buddy",
            species = "Dog",
            breed = "Golden Retriever",
            age = 3,
            owner = "John Doe",
            registrationDate = registrationDate
        )
        
        val pet2 = Pet(
            id = "pet-123",
            name = "Buddy",
            species = "Dog",
            breed = "Golden Retriever",
            age = 3,
            owner = "John Doe",
            registrationDate = registrationDate
        )

        // Then
        assertEquals(pet1, pet2)
        assertEquals(pet1.hashCode(), pet2.hashCode())
    }

    @Test
    fun `Pet equality should work correctly with different data`() {
        // Given
        val pet1 = Pet(
            id = "pet-123",
            name = "Buddy",
            species = "Dog",
            breed = "Golden Retriever",
            age = 3,
            owner = "John Doe",
            registrationDate = LocalDate.now()
        )
        
        val pet2 = Pet(
            id = "pet-456",
            name = "Mittens",
            species = "Cat",
            breed = "Persian",
            age = 2,
            owner = "Jane Smith",
            registrationDate = LocalDate.now()
        )

        // Then
        assertNotEquals(pet1, pet2)
        assertNotEquals(pet1.hashCode(), pet2.hashCode())
    }

    @Test
    fun `Pet equality should work correctly with different ids`() {
        // Given
        val registrationDate = LocalDate.now()
        val pet1 = Pet(
            id = "pet-123",
            name = "Buddy",
            species = "Dog",
            breed = "Golden Retriever",
            age = 3,
            owner = "John Doe",
            registrationDate = registrationDate
        )
        
        val pet2 = Pet(
            id = "pet-456",
            name = "Buddy",
            species = "Dog",
            breed = "Golden Retriever",
            age = 3,
            owner = "John Doe",
            registrationDate = registrationDate
        )

        // Then
        assertNotEquals(pet1, pet2)
    }

    @Test
    fun `Pet should support component destructuring`() {
        // Given
        val pet = Pet(
            id = "pet-123",
            name = "Buddy",
            species = "Dog",
            breed = "Golden Retriever",
            age = 3,
            owner = "John Doe",
            registrationDate = LocalDate.of(2023, 11, 20)
        )

        // When
        val (id, name, species, breed, age, owner, registrationDate) = pet

        // Then
        assertEquals("pet-123", id)
        assertEquals("Buddy", name)
        assertEquals("Dog", species)
        assertEquals("Golden Retriever", breed)
        assertEquals(3, age)
        assertEquals("John Doe", owner)
        assertEquals(LocalDate.of(2023, 11, 20), registrationDate)
    }

    @Test
    fun `Pet copy should work correctly`() {
        // Given
        val originalPet = Pet(
            id = "pet-123",
            name = "Buddy",
            species = "Dog",
            breed = "Golden Retriever",
            age = 3,
            owner = "John Doe",
            registrationDate = LocalDate.now()
        )

        // When
        val copiedPet = originalPet.copy(age = 4, owner = "Jane Doe")

        // Then
        assertEquals(originalPet.id, copiedPet.id)
        assertEquals(originalPet.name, copiedPet.name)
        assertEquals(originalPet.species, copiedPet.species)
        assertEquals(originalPet.breed, copiedPet.breed)
        assertEquals(4, copiedPet.age) // Changed
        assertEquals("Jane Doe", copiedPet.owner) // Changed
        assertEquals(originalPet.registrationDate, copiedPet.registrationDate)
    }

    @Test
    fun `Pet toString should include all fields`() {
        // Given
        val pet = Pet(
            id = "pet-123",
            name = "Buddy",
            species = "Dog",
            breed = "Golden Retriever",
            age = 3,
            owner = "John Doe",
            registrationDate = LocalDate.of(2023, 12, 25)
        )

        // When
        val toStringResult = pet.toString()

        // Then
        assert(toStringResult.contains("pet-123"))
        assert(toStringResult.contains("Buddy"))
        assert(toStringResult.contains("Dog"))
        assert(toStringResult.contains("Golden Retriever"))
        assert(toStringResult.contains("3"))
        assert(toStringResult.contains("John Doe"))
        assert(toStringResult.contains("2023-12-25"))
    }
}