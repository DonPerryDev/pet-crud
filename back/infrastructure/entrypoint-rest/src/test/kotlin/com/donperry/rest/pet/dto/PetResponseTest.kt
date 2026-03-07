package com.donperry.rest.pet.dto

import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.time.LocalDate
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertNull

class PetResponseTest {

    @Test
    fun `PetResponse should be created with all required fields`() {
        val id = "pet-123"
        val name = "Buddy"
        val species = "Dog"
        val breed = "Golden Retriever"
        val age = 3
        val owner = "John Doe"
        val registrationDate = LocalDate.of(2023, 12, 25)

        val response = PetResponse(
            id = id,
            name = name,
            species = species,
            breed = breed,
            age = age,
            birthdate = null,
            weight = null,
            nickname = null,
            owner = owner,
            registrationDate = registrationDate,
            photoUrl = null
        )

        assertEquals(id, response.id)
        assertEquals(name, response.name)
        assertEquals(species, response.species)
        assertEquals(breed, response.breed)
        assertEquals(age, response.age)
        assertNull(response.birthdate)
        assertNull(response.weight)
        assertNull(response.nickname)
        assertEquals(owner, response.owner)
        assertEquals(registrationDate, response.registrationDate)
        assertNull(response.photoUrl)
    }

    @Test
    fun `PetResponse should handle null breed`() {
        val response = PetResponse(
            id = "pet-456",
            name = "Mittens",
            species = "Cat",
            breed = null,
            age = 2,
            birthdate = null,
            weight = null,
            nickname = null,
            owner = "Jane Smith",
            registrationDate = LocalDate.now(),
            photoUrl = null
        )

        assertEquals("pet-456", response.id)
        assertEquals("Mittens", response.name)
        assertEquals("Cat", response.species)
        assertNull(response.breed)
        assertEquals(2, response.age)
        assertNull(response.birthdate)
        assertNull(response.weight)
        assertNull(response.nickname)
        assertEquals("Jane Smith", response.owner)
        assertNull(response.photoUrl)
    }

    @Test
    fun `PetResponse should handle empty string breed`() {
        val response = PetResponse(
            id = "pet-789",
            name = "Rex",
            species = "Dog",
            breed = "",
            age = 5,
            birthdate = null,
            weight = null,
            nickname = null,
            owner = "Bob Wilson",
            registrationDate = LocalDate.now(),
            photoUrl = null
        )

        assertEquals("pet-789", response.id)
        assertEquals("Rex", response.name)
        assertEquals("Dog", response.species)
        assertEquals("", response.breed)
        assertEquals(5, response.age)
        assertNull(response.birthdate)
        assertNull(response.weight)
        assertNull(response.nickname)
        assertEquals("Bob Wilson", response.owner)
        assertNull(response.photoUrl)
    }

    @Test
    fun `PetResponse should handle zero age`() {
        val response = PetResponse(
            id = "pet-000",
            name = "NewBorn",
            species = "Dog",
            breed = "Syrian",
            age = 0,
            birthdate = null,
            weight = null,
            nickname = null,
            owner = "Pet Store",
            registrationDate = LocalDate.now(),
            photoUrl = null
        )

        assertEquals("pet-000", response.id)
        assertEquals("NewBorn", response.name)
        assertEquals("Dog", response.species)
        assertEquals("Syrian", response.breed)
        assertEquals(0, response.age)
        assertNull(response.birthdate)
        assertNull(response.weight)
        assertNull(response.nickname)
        assertEquals("Pet Store", response.owner)
        assertNull(response.photoUrl)
    }

    @Test
    fun `PetResponse equality should work correctly with same data`() {
        val registrationDate = LocalDate.of(2023, 10, 15)
        val response1 = PetResponse(
            id = "pet-123",
            name = "Buddy",
            species = "Dog",
            breed = "Golden Retriever",
            age = 3,
            birthdate = null,
            weight = null,
            nickname = null,
            owner = "John Doe",
            registrationDate = registrationDate,
            photoUrl = null
        )

        val response2 = PetResponse(
            id = "pet-123",
            name = "Buddy",
            species = "Dog",
            breed = "Golden Retriever",
            age = 3,
            birthdate = null,
            weight = null,
            nickname = null,
            owner = "John Doe",
            registrationDate = registrationDate,
            photoUrl = null
        )

        assertEquals(response1, response2)
        assertEquals(response1.hashCode(), response2.hashCode())
    }

    @Test
    fun `PetResponse equality should work correctly with different data`() {
        val response1 = PetResponse(
            id = "pet-123",
            name = "Buddy",
            species = "Dog",
            breed = "Golden Retriever",
            age = 3,
            birthdate = null,
            weight = null,
            nickname = null,
            owner = "John Doe",
            registrationDate = LocalDate.of(2023, 10, 15),
            photoUrl = null
        )

        val response2 = PetResponse(
            id = "pet-456",
            name = "Mittens",
            species = "Cat",
            breed = "Persian",
            age = 2,
            birthdate = null,
            weight = null,
            nickname = null,
            owner = "Jane Smith",
            registrationDate = LocalDate.of(2023, 11, 20),
            photoUrl = null
        )

        assertNotEquals(response1, response2)
        assertNotEquals(response1.hashCode(), response2.hashCode())
    }

    @Test
    fun `PetResponse equality should work correctly with different ids`() {
        val registrationDate = LocalDate.now()
        val response1 = PetResponse(
            id = "pet-123",
            name = "Buddy",
            species = "Dog",
            breed = "Golden Retriever",
            age = 3,
            birthdate = null,
            weight = null,
            nickname = null,
            owner = "John Doe",
            registrationDate = registrationDate,
            photoUrl = null
        )

        val response2 = PetResponse(
            id = "pet-456",
            name = "Buddy",
            species = "Dog",
            breed = "Golden Retriever",
            age = 3,
            birthdate = null,
            weight = null,
            nickname = null,
            owner = "John Doe",
            registrationDate = registrationDate,
            photoUrl = null
        )

        assertNotEquals(response1, response2)
    }

    @Test
    fun `PetResponse should support component destructuring`() {
        val response = PetResponse(
            id = "pet-123",
            name = "Charlie",
            species = "Dog",
            breed = "Labrador",
            age = 4,
            birthdate = LocalDate.of(2020, 1, 1),
            weight = BigDecimal("25.5"),
            nickname = "Chuck",
            owner = "Mike Johnson",
            registrationDate = LocalDate.of(2023, 11, 20),
            photoUrl = "https://example.com/photo.jpg"
        )

        val (id, name, species, breed, age, birthdate, weight, nickname, owner, registrationDate, photoUrl) = response

        assertEquals("pet-123", id)
        assertEquals("Charlie", name)
        assertEquals("Dog", species)
        assertEquals("Labrador", breed)
        assertEquals(4, age)
        assertEquals(LocalDate.of(2020, 1, 1), birthdate)
        assertEquals(BigDecimal("25.5"), weight)
        assertEquals("Chuck", nickname)
        assertEquals("Mike Johnson", owner)
        assertEquals(LocalDate.of(2023, 11, 20), registrationDate)
        assertEquals("https://example.com/photo.jpg", photoUrl)
    }

    @Test
    fun `PetResponse copy should work correctly`() {
        val originalResponse = PetResponse(
            id = "pet-123",
            name = "Buddy",
            species = "Dog",
            breed = "Golden Retriever",
            age = 3,
            birthdate = null,
            weight = null,
            nickname = null,
            owner = "John Doe",
            registrationDate = LocalDate.now(),
            photoUrl = null
        )

        val copiedResponse = originalResponse.copy(age = 4, owner = "Jane Doe")

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
        val response = PetResponse(
            id = "pet-123",
            name = "Buddy",
            species = "Dog",
            breed = "Golden Retriever",
            age = 3,
            birthdate = LocalDate.of(2020, 5, 15),
            weight = BigDecimal("30.0"),
            nickname = "Bud",
            owner = "John Doe",
            registrationDate = LocalDate.of(2023, 12, 25),
            photoUrl = "https://example.com/photo.jpg"
        )

        val toStringResult = response.toString()

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
        val futureDate = LocalDate.of(2025, 1, 1)
        val pastDate = LocalDate.of(2020, 6, 15)

        val futureResponse = PetResponse(
            id = "pet-future",
            name = "Future Pet",
            species = "Dog",
            breed = "AI",
            age = 1,
            birthdate = null,
            weight = null,
            nickname = null,
            owner = "Future Owner",
            registrationDate = futureDate,
            photoUrl = null
        )

        val pastResponse = PetResponse(
            id = "pet-past",
            name = "Past Pet",
            species = "Dog",
            breed = "Ancient",
            age = 10,
            birthdate = null,
            weight = null,
            nickname = null,
            owner = "Past Owner",
            registrationDate = pastDate,
            photoUrl = null
        )

        assertEquals(futureDate, futureResponse.registrationDate)
        assertEquals(pastDate, pastResponse.registrationDate)
    }

    @Test
    fun `PetResponse should handle UUID-style ids`() {
        val response = PetResponse(
            id = "550e8400-e29b-41d4-a716-446655440000",
            name = "UUID Pet",
            species = "Cat",
            breed = "Digital",
            age = 2,
            birthdate = null,
            weight = null,
            nickname = null,
            owner = "Tech Owner",
            registrationDate = LocalDate.now(),
            photoUrl = null
        )

        assertEquals("550e8400-e29b-41d4-a716-446655440000", response.id)
        assertEquals("UUID Pet", response.name)
    }

    @Test
    fun `PetResponse should handle new optional fields`() {
        val response = PetResponse(
            id = "pet-123",
            name = "Max",
            species = "Dog",
            breed = "Beagle",
            age = 2,
            birthdate = LocalDate.of(2022, 3, 10),
            weight = BigDecimal("15.7"),
            nickname = "Maxie",
            owner = "Owner Name",
            registrationDate = LocalDate.now(),
            photoUrl = "https://example.com/max.jpg"
        )

        assertEquals("Max", response.name)
        assertEquals(LocalDate.of(2022, 3, 10), response.birthdate)
        assertEquals(BigDecimal("15.7"), response.weight)
        assertEquals("Maxie", response.nickname)
        assertEquals("https://example.com/max.jpg", response.photoUrl)
    }
}
