package com.donperry.rest.pet.dto

import org.junit.jupiter.api.Test
import java.time.LocalDate
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertNull

class PetResponseTest {

    @Test
    fun `PetResponse should be created with all required fields`() {
        // Given
        val id = "pet-123"
        val name = "Buddy"
        val species = "Dog"
        val breed = "Golden Retriever"
        val age = 3
        val owner = "John Doe"
        val registrationDate = LocalDate.of(2023, 12, 25)

        // When
        val response = PetResponse(
            id = id,
            name = name,
            species = species,
            breed = breed,
            age = age,
            owner = owner,
            registrationDate = registrationDate
        )

        // Then
        assertEquals(id, response.id)
        assertEquals(name, response.name)
        assertEquals(species, response.species)
        assertEquals(breed, response.breed)
        assertEquals(age, response.age)
        assertEquals(owner, response.owner)
        assertEquals(registrationDate, response.registrationDate)
    }

    @Test
    fun `PetResponse should handle null breed`() {
        // When
        val response = PetResponse(
            id = "pet-456",
            name = "Mittens",
            species = "Cat",
            breed = null,
            age = 2,
            owner = "Jane Smith",
            registrationDate = LocalDate.now()
        )

        // Then
        assertEquals("pet-456", response.id)
        assertEquals("Mittens", response.name)
        assertEquals("Cat", response.species)
        assertNull(response.breed)
        assertEquals(2, response.age)
        assertEquals("Jane Smith", response.owner)
    }

    @Test
    fun `PetResponse should handle empty string breed`() {
        // When
        val response = PetResponse(
            id = "pet-789",
            name = "Rex",
            species = "Dog",
            breed = "",
            age = 5,
            owner = "Bob Wilson",
            registrationDate = LocalDate.now()
        )

        // Then
        assertEquals("pet-789", response.id)
        assertEquals("Rex", response.name)
        assertEquals("Dog", response.species)
        assertEquals("", response.breed)
        assertEquals(5, response.age)
        assertEquals("Bob Wilson", response.owner)
    }

    @Test
    fun `PetResponse should handle zero age`() {
        // When
        val response = PetResponse(
            id = "pet-000",
            name = "NewBorn",
            species = "Hamster",
            breed = "Syrian",
            age = 0,
            owner = "Pet Store",
            registrationDate = LocalDate.now()
        )

        // Then
        assertEquals("pet-000", response.id)
        assertEquals("NewBorn", response.name)
        assertEquals("Hamster", response.species)
        assertEquals("Syrian", response.breed)
        assertEquals(0, response.age)
        assertEquals("Pet Store", response.owner)
    }

    @Test
    fun `PetResponse equality should work correctly with same data`() {
        // Given
        val registrationDate = LocalDate.of(2023, 10, 15)
        val response1 = PetResponse(
            id = "pet-123",
            name = "Buddy",
            species = "Dog",
            breed = "Golden Retriever",
            age = 3,
            owner = "John Doe",
            registrationDate = registrationDate
        )
        
        val response2 = PetResponse(
            id = "pet-123",
            name = "Buddy",
            species = "Dog",
            breed = "Golden Retriever",
            age = 3,
            owner = "John Doe",
            registrationDate = registrationDate
        )

        // Then
        assertEquals(response1, response2)
        assertEquals(response1.hashCode(), response2.hashCode())
    }

    @Test
    fun `PetResponse equality should work correctly with different data`() {
        // Given
        val response1 = PetResponse(
            id = "pet-123",
            name = "Buddy",
            species = "Dog",
            breed = "Golden Retriever",
            age = 3,
            owner = "John Doe",
            registrationDate = LocalDate.of(2023, 10, 15)
        )
        
        val response2 = PetResponse(
            id = "pet-456",
            name = "Mittens",
            species = "Cat",
            breed = "Persian",
            age = 2,
            owner = "Jane Smith",
            registrationDate = LocalDate.of(2023, 11, 20)
        )

        // Then
        assertNotEquals(response1, response2)
        assertNotEquals(response1.hashCode(), response2.hashCode())
    }

    @Test
    fun `PetResponse equality should work correctly with different ids`() {
        // Given
        val registrationDate = LocalDate.now()
        val response1 = PetResponse(
            id = "pet-123",
            name = "Buddy",
            species = "Dog",
            breed = "Golden Retriever",
            age = 3,
            owner = "John Doe",
            registrationDate = registrationDate
        )
        
        val response2 = PetResponse(
            id = "pet-456",
            name = "Buddy",
            species = "Dog",
            breed = "Golden Retriever",
            age = 3,
            owner = "John Doe",
            registrationDate = registrationDate
        )

        // Then
        assertNotEquals(response1, response2)
    }

    @Test
    fun `PetResponse should support component destructuring`() {
        // Given
        val response = PetResponse(
            id = "pet-123",
            name = "Charlie",
            species = "Dog",
            breed = "Labrador",
            age = 4,
            owner = "Mike Johnson",
            registrationDate = LocalDate.of(2023, 11, 20)
        )

        // When
        val (id, name, species, breed, age, owner, registrationDate) = response

        // Then
        assertEquals("pet-123", id)
        assertEquals("Charlie", name)
        assertEquals("Dog", species)
        assertEquals("Labrador", breed)
        assertEquals(4, age)
        assertEquals("Mike Johnson", owner)
        assertEquals(LocalDate.of(2023, 11, 20), registrationDate)
    }

    @Test
    fun `PetResponse copy should work correctly`() {
        // Given
        val originalResponse = PetResponse(
            id = "pet-123",
            name = "Buddy",
            species = "Dog",
            breed = "Golden Retriever",
            age = 3,
            owner = "John Doe",
            registrationDate = LocalDate.now()
        )

        // When
        val copiedResponse = originalResponse.copy(age = 4, owner = "Jane Doe")

        // Then
        assertEquals(originalResponse.id, copiedResponse.id)
        assertEquals(originalResponse.name, copiedResponse.name)
        assertEquals(originalResponse.species, copiedResponse.species)
        assertEquals(originalResponse.breed, copiedResponse.breed)
        assertEquals(4, copiedResponse.age) // Changed
        assertEquals("Jane Doe", copiedResponse.owner) // Changed
        assertEquals(originalResponse.registrationDate, copiedResponse.registrationDate)
    }

    @Test
    fun `PetResponse toString should include all fields`() {
        // Given
        val response = PetResponse(
            id = "pet-123",
            name = "Buddy",
            species = "Dog",
            breed = "Golden Retriever",
            age = 3,
            owner = "John Doe",
            registrationDate = LocalDate.of(2023, 12, 25)
        )

        // When
        val toStringResult = response.toString()

        // Then
        assert(toStringResult.contains("pet-123"))
        assert(toStringResult.contains("Buddy"))
        assert(toStringResult.contains("Dog"))
        assert(toStringResult.contains("Golden Retriever"))
        assert(toStringResult.contains("3"))
        assert(toStringResult.contains("John Doe"))
        assert(toStringResult.contains("2023-12-25"))
    }

    @Test
    fun `PetResponse should handle different date formats`() {
        // Given
        val futureDate = LocalDate.of(2025, 1, 1)
        val pastDate = LocalDate.of(2020, 6, 15)

        // When
        val futureResponse = PetResponse(
            id = "pet-future",
            name = "Future Pet",
            species = "Robot",
            breed = "AI",
            age = 1,
            owner = "Future Owner",
            registrationDate = futureDate
        )

        val pastResponse = PetResponse(
            id = "pet-past",
            name = "Past Pet",
            species = "Dog",
            breed = "Ancient",
            age = 10,
            owner = "Past Owner",
            registrationDate = pastDate
        )

        // Then
        assertEquals(futureDate, futureResponse.registrationDate)
        assertEquals(pastDate, pastResponse.registrationDate)
    }

    @Test
    fun `PetResponse should handle UUID-style ids`() {
        // When
        val response = PetResponse(
            id = "550e8400-e29b-41d4-a716-446655440000",
            name = "UUID Pet",
            species = "Cat",
            breed = "Digital",
            age = 2,
            owner = "Tech Owner",
            registrationDate = LocalDate.now()
        )

        // Then
        assertEquals("550e8400-e29b-41d4-a716-446655440000", response.id)
        assertEquals("UUID Pet", response.name)
    }
}