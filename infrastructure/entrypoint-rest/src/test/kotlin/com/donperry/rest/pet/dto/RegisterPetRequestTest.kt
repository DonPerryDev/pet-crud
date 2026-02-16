package com.donperry.rest.pet.dto

import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.time.LocalDate
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertNull

class RegisterPetRequestTest {

    @Test
    fun `RegisterPetRequest should be created with all required fields`() {
        val request = RegisterPetRequest(
            name = "Buddy",
            species = "Dog",
            breed = "Golden Retriever",
            age = 3,
            birthdate = null,
            weight = null,
            nickname = null
        )

        assertEquals("Buddy", request.name)
        assertEquals("Dog", request.species)
        assertEquals("Golden Retriever", request.breed)
        assertEquals(3, request.age)
        assertNull(request.birthdate)
        assertNull(request.weight)
        assertNull(request.nickname)
    }

    @Test
    fun `RegisterPetRequest should handle null breed`() {
        val request = RegisterPetRequest(
            name = "Mittens",
            species = "Cat",
            breed = null,
            age = 2,
            birthdate = null,
            weight = null,
            nickname = null
        )

        assertEquals("Mittens", request.name)
        assertEquals("Cat", request.species)
        assertNull(request.breed)
        assertEquals(2, request.age)
        assertNull(request.birthdate)
        assertNull(request.weight)
        assertNull(request.nickname)
    }

    @Test
    fun `RegisterPetRequest should handle empty string breed`() {
        val request = RegisterPetRequest(
            name = "Rex",
            species = "Dog",
            breed = "",
            age = 5,
            birthdate = null,
            weight = null,
            nickname = null
        )

        assertEquals("Rex", request.name)
        assertEquals("Dog", request.species)
        assertEquals("", request.breed)
        assertEquals(5, request.age)
        assertNull(request.birthdate)
        assertNull(request.weight)
        assertNull(request.nickname)
    }

    @Test
    fun `RegisterPetRequest should handle zero age`() {
        val request = RegisterPetRequest(
            name = "NewBorn",
            species = "Dog",
            breed = "Syrian",
            age = 0,
            birthdate = null,
            weight = null,
            nickname = null
        )

        assertEquals("NewBorn", request.name)
        assertEquals("Dog", request.species)
        assertEquals("Syrian", request.breed)
        assertEquals(0, request.age)
        assertNull(request.birthdate)
        assertNull(request.weight)
        assertNull(request.nickname)
    }

    @Test
    fun `RegisterPetRequest equality should work correctly with same data`() {
        val request1 = RegisterPetRequest(
            name = "Buddy",
            species = "Dog",
            breed = "Golden Retriever",
            age = 3,
            birthdate = null,
            weight = null,
            nickname = null
        )

        val request2 = RegisterPetRequest(
            name = "Buddy",
            species = "Dog",
            breed = "Golden Retriever",
            age = 3,
            birthdate = null,
            weight = null,
            nickname = null
        )

        assertEquals(request1, request2)
        assertEquals(request1.hashCode(), request2.hashCode())
    }

    @Test
    fun `RegisterPetRequest equality should work correctly with different data`() {
        val request1 = RegisterPetRequest(
            name = "Buddy",
            species = "Dog",
            breed = "Golden Retriever",
            age = 3,
            birthdate = null,
            weight = null,
            nickname = null
        )

        val request2 = RegisterPetRequest(
            name = "Mittens",
            species = "Cat",
            breed = "Persian",
            age = 2,
            birthdate = null,
            weight = null,
            nickname = null
        )

        assertNotEquals(request1, request2)
        assertNotEquals(request1.hashCode(), request2.hashCode())
    }

    @Test
    fun `RegisterPetRequest should support component destructuring`() {
        val request = RegisterPetRequest(
            name = "Charlie",
            species = "Dog",
            breed = "Labrador",
            age = 4,
            birthdate = LocalDate.of(2020, 1, 1),
            weight = BigDecimal("25.5"),
            nickname = "Chuck"
        )

        val (name, species, breed, age, birthdate, weight, nickname) = request

        assertEquals("Charlie", name)
        assertEquals("Dog", species)
        assertEquals("Labrador", breed)
        assertEquals(4, age)
        assertEquals(LocalDate.of(2020, 1, 1), birthdate)
        assertEquals(BigDecimal("25.5"), weight)
        assertEquals("Chuck", nickname)
    }

    @Test
    fun `RegisterPetRequest copy should work correctly`() {
        val originalRequest = RegisterPetRequest(
            name = "Buddy",
            species = "Dog",
            breed = "Golden Retriever",
            age = 3,
            birthdate = null,
            weight = null,
            nickname = null
        )

        val copiedRequest = originalRequest.copy(age = 4, nickname = "Bud")

        assertEquals(originalRequest.name, copiedRequest.name)
        assertEquals(originalRequest.species, copiedRequest.species)
        assertEquals(originalRequest.breed, copiedRequest.breed)
        assertEquals(4, copiedRequest.age) // Changed
        assertEquals("Bud", copiedRequest.nickname) // Changed
    }

    @Test
    fun `RegisterPetRequest toString should include all fields`() {
        val request = RegisterPetRequest(
            name = "Buddy",
            species = "Dog",
            breed = "Golden Retriever",
            age = 3,
            birthdate = LocalDate.of(2020, 5, 15),
            weight = BigDecimal("30.0"),
            nickname = "Bud"
        )

        val toStringResult = request.toString()

        assert(toStringResult.contains("Buddy"))
        assert(toStringResult.contains("Dog"))
        assert(toStringResult.contains("Golden Retriever"))
        assert(toStringResult.contains("3"))
        assert(toStringResult.contains("2020-05-15"))
        assert(toStringResult.contains("30.0"))
        assert(toStringResult.contains("Bud"))
    }

    @Test
    fun `RegisterPetRequest should handle special characters in strings`() {
        val request = RegisterPetRequest(
            name = "Señor Fluffington",
            species = "Cat",
            breed = "Maine Coon",
            age = 7,
            birthdate = null,
            weight = null,
            nickname = "Señor"
        )

        assertEquals("Señor Fluffington", request.name)
        assertEquals("Cat", request.species)
        assertEquals("Maine Coon", request.breed)
        assertEquals(7, request.age)
        assertEquals("Señor", request.nickname)
    }

    @Test
    fun `RegisterPetRequest should handle long strings`() {
        val longName = "A".repeat(100)
        val longBreed = "B".repeat(200)

        val request = RegisterPetRequest(
            name = longName,
            species = "Dog",
            breed = longBreed,
            age = 1,
            birthdate = null,
            weight = null,
            nickname = null
        )

        assertEquals(longName, request.name)
        assertEquals("Dog", request.species)
        assertEquals(longBreed, request.breed)
        assertEquals(1, request.age)
    }

    @Test
    fun `RegisterPetRequest should handle new optional fields`() {
        val request = RegisterPetRequest(
            name = "Max",
            species = "Dog",
            breed = "Beagle",
            age = 2,
            birthdate = LocalDate.of(2022, 3, 10),
            weight = BigDecimal("15.7"),
            nickname = "Maxie"
        )

        assertEquals("Max", request.name)
        assertEquals("Dog", request.species)
        assertEquals("Beagle", request.breed)
        assertEquals(2, request.age)
        assertEquals(LocalDate.of(2022, 3, 10), request.birthdate)
        assertEquals(BigDecimal("15.7"), request.weight)
        assertEquals("Maxie", request.nickname)
    }
}
