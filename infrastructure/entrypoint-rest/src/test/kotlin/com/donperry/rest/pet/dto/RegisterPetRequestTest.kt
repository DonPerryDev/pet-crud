package com.donperry.rest.pet.dto

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertNull

class RegisterPetRequestTest {

    @Test
    fun `RegisterPetRequest should be created with all required fields`() {
        // When
        val request = RegisterPetRequest(
            name = "Buddy",
            species = "Dog",
            breed = "Golden Retriever",
            age = 3,
            owner = "John Doe"
        )

        // Then
        assertEquals("Buddy", request.name)
        assertEquals("Dog", request.species)
        assertEquals("Golden Retriever", request.breed)
        assertEquals(3, request.age)
        assertEquals("John Doe", request.owner)
    }

    @Test
    fun `RegisterPetRequest should handle null breed`() {
        // When
        val request = RegisterPetRequest(
            name = "Mittens",
            species = "Cat",
            breed = null,
            age = 2,
            owner = "Jane Smith"
        )

        // Then
        assertEquals("Mittens", request.name)
        assertEquals("Cat", request.species)
        assertNull(request.breed)
        assertEquals(2, request.age)
        assertEquals("Jane Smith", request.owner)
    }

    @Test
    fun `RegisterPetRequest should handle empty string breed`() {
        // When
        val request = RegisterPetRequest(
            name = "Rex",
            species = "Dog",
            breed = "",
            age = 5,
            owner = "Bob Wilson"
        )

        // Then
        assertEquals("Rex", request.name)
        assertEquals("Dog", request.species)
        assertEquals("", request.breed)
        assertEquals(5, request.age)
        assertEquals("Bob Wilson", request.owner)
    }

    @Test
    fun `RegisterPetRequest should handle zero age`() {
        // When
        val request = RegisterPetRequest(
            name = "NewBorn",
            species = "Hamster",
            breed = "Syrian",
            age = 0,
            owner = "Pet Store"
        )

        // Then
        assertEquals("NewBorn", request.name)
        assertEquals("Hamster", request.species)
        assertEquals("Syrian", request.breed)
        assertEquals(0, request.age)
        assertEquals("Pet Store", request.owner)
    }

    @Test
    fun `RegisterPetRequest equality should work correctly with same data`() {
        // Given
        val request1 = RegisterPetRequest(
            name = "Buddy",
            species = "Dog",
            breed = "Golden Retriever",
            age = 3,
            owner = "John Doe"
        )
        
        val request2 = RegisterPetRequest(
            name = "Buddy",
            species = "Dog",
            breed = "Golden Retriever",
            age = 3,
            owner = "John Doe"
        )

        // Then
        assertEquals(request1, request2)
        assertEquals(request1.hashCode(), request2.hashCode())
    }

    @Test
    fun `RegisterPetRequest equality should work correctly with different data`() {
        // Given
        val request1 = RegisterPetRequest(
            name = "Buddy",
            species = "Dog",
            breed = "Golden Retriever",
            age = 3,
            owner = "John Doe"
        )
        
        val request2 = RegisterPetRequest(
            name = "Mittens",
            species = "Cat",
            breed = "Persian",
            age = 2,
            owner = "Jane Smith"
        )

        // Then
        assertNotEquals(request1, request2)
        assertNotEquals(request1.hashCode(), request2.hashCode())
    }

    @Test
    fun `RegisterPetRequest should support component destructuring`() {
        // Given
        val request = RegisterPetRequest(
            name = "Charlie",
            species = "Dog",
            breed = "Labrador",
            age = 4,
            owner = "Mike Johnson"
        )

        // When
        val (name, species, breed, age, owner) = request

        // Then
        assertEquals("Charlie", name)
        assertEquals("Dog", species)
        assertEquals("Labrador", breed)
        assertEquals(4, age)
        assertEquals("Mike Johnson", owner)
    }

    @Test
    fun `RegisterPetRequest copy should work correctly`() {
        // Given
        val originalRequest = RegisterPetRequest(
            name = "Buddy",
            species = "Dog",
            breed = "Golden Retriever",
            age = 3,
            owner = "John Doe"
        )

        // When
        val copiedRequest = originalRequest.copy(age = 4, owner = "Jane Doe")

        // Then
        assertEquals(originalRequest.name, copiedRequest.name)
        assertEquals(originalRequest.species, copiedRequest.species)
        assertEquals(originalRequest.breed, copiedRequest.breed)
        assertEquals(4, copiedRequest.age) // Changed
        assertEquals("Jane Doe", copiedRequest.owner) // Changed
    }

    @Test
    fun `RegisterPetRequest toString should include all fields`() {
        // Given
        val request = RegisterPetRequest(
            name = "Buddy",
            species = "Dog",
            breed = "Golden Retriever",
            age = 3,
            owner = "John Doe"
        )

        // When
        val toStringResult = request.toString()

        // Then
        assert(toStringResult.contains("Buddy"))
        assert(toStringResult.contains("Dog"))
        assert(toStringResult.contains("Golden Retriever"))
        assert(toStringResult.contains("3"))
        assert(toStringResult.contains("John Doe"))
    }

    @Test
    fun `RegisterPetRequest should handle special characters in strings`() {
        // When
        val request = RegisterPetRequest(
            name = "Señor Fluffington",
            species = "Cat",
            breed = "Maine Coon",
            age = 7,
            owner = "María José González"
        )

        // Then
        assertEquals("Señor Fluffington", request.name)
        assertEquals("Cat", request.species)
        assertEquals("Maine Coon", request.breed)
        assertEquals(7, request.age)
        assertEquals("María José González", request.owner)
    }

    @Test
    fun `RegisterPetRequest should handle long strings`() {
        // Given
        val longName = "A".repeat(100)
        val longBreed = "B".repeat(200)
        val longOwner = "C".repeat(150)

        // When
        val request = RegisterPetRequest(
            name = longName,
            species = "Dog",
            breed = longBreed,
            age = 1,
            owner = longOwner
        )

        // Then
        assertEquals(longName, request.name)
        assertEquals("Dog", request.species)
        assertEquals(longBreed, request.breed)
        assertEquals(1, request.age)
        assertEquals(longOwner, request.owner)
    }
}