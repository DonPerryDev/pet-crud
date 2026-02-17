package com.donperry.rest.pet.dto

import com.donperry.model.pet.Species
import com.donperry.rest.common.validation.Validated
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.time.LocalDate
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class UpdatePetRequestTest {

    @Test
    fun `should return Valid when all fields are correct`() {
        val request = UpdatePetRequest(
            name = "Buddy",
            species = "DOG",
            breed = "Golden Retriever",
            age = 3,
            birthdate = LocalDate.of(2021, 1, 15),
            weight = BigDecimal("25.5"),
            nickname = "Bud",
        )

        val result = request.validate("pet-123", "user-123")

        assertTrue(result is Validated.Valid)
        val command = result.value
        assertEquals("pet-123", command.petId)
        assertEquals("user-123", command.userId)
        assertEquals("Buddy", command.name)
        assertEquals(Species.DOG, command.species)
        assertEquals("Golden Retriever", command.breed)
        assertEquals(3, command.age)
        assertEquals(LocalDate.of(2021, 1, 15), command.birthdate)
        assertEquals(BigDecimal("25.5"), command.weight)
        assertEquals("Bud", command.nickname)
    }

    @Test
    fun `should return Invalid when petId is blank`() {
        val request = UpdatePetRequest(
            name = "Buddy",
            species = "DOG",
            breed = "Golden Retriever",
            age = 3,
            birthdate = null,
            weight = null,
            nickname = null,
        )

        val result = request.validate("", "user-123")

        assertTrue(result is Validated.Invalid)
        assertEquals("Pet ID cannot be blank", result.error)
    }

    @Test
    fun `should return Invalid when name is blank`() {
        val request = UpdatePetRequest(
            name = "",
            species = "DOG",
            breed = "Golden Retriever",
            age = 3,
            birthdate = null,
            weight = null,
            nickname = null,
        )

        val result = request.validate("pet-123", "user-123")

        assertTrue(result is Validated.Invalid)
        assertEquals("Pet name cannot be blank", result.error)
    }

    @Test
    fun `should return Invalid when species is blank`() {
        val request = UpdatePetRequest(
            name = "Buddy",
            species = "",
            breed = "Golden Retriever",
            age = 3,
            birthdate = null,
            weight = null,
            nickname = null,
        )

        val result = request.validate("pet-123", "user-123")

        assertTrue(result is Validated.Invalid)
        assertEquals("Pet species cannot be blank", result.error)
    }

    @Test
    fun `should return Invalid when species is invalid enum value`() {
        val request = UpdatePetRequest(
            name = "Buddy",
            species = "LIZARD",
            breed = null,
            age = 3,
            birthdate = null,
            weight = null,
            nickname = null,
        )

        val result = request.validate("pet-123", "user-123")

        assertTrue(result is Validated.Invalid)
        assertTrue(result.error.contains("Invalid species: LIZARD"))
        assertTrue(result.error.contains("Must be one of: DOG, CAT"))
    }

    @Test
    fun `should return Invalid when age is negative`() {
        val request = UpdatePetRequest(
            name = "Buddy",
            species = "DOG",
            breed = "Golden Retriever",
            age = -1,
            birthdate = null,
            weight = null,
            nickname = null,
        )

        val result = request.validate("pet-123", "user-123")

        assertTrue(result is Validated.Invalid)
        assertEquals("Pet age must be zero or greater", result.error)
    }

    @Test
    fun `should return Invalid when weight is zero`() {
        val request = UpdatePetRequest(
            name = "Buddy",
            species = "DOG",
            breed = "Golden Retriever",
            age = 3,
            birthdate = null,
            weight = BigDecimal.ZERO,
            nickname = null,
        )

        val result = request.validate("pet-123", "user-123")

        assertTrue(result is Validated.Invalid)
        assertEquals("Pet weight must be greater than zero", result.error)
    }

    @Test
    fun `should return Invalid when weight is negative`() {
        val request = UpdatePetRequest(
            name = "Buddy",
            species = "DOG",
            breed = "Golden Retriever",
            age = 3,
            birthdate = null,
            weight = BigDecimal("-5.0"),
            nickname = null,
        )

        val result = request.validate("pet-123", "user-123")

        assertTrue(result is Validated.Invalid)
        assertEquals("Pet weight must be greater than zero", result.error)
    }

    @Test
    fun `should return Invalid when breed is blank string`() {
        val request = UpdatePetRequest(
            name = "Buddy",
            species = "DOG",
            breed = "",
            age = 3,
            birthdate = null,
            weight = null,
            nickname = null,
        )

        val result = request.validate("pet-123", "user-123")

        assertTrue(result is Validated.Invalid)
        assertEquals("Pet breed cannot be blank", result.error)
    }

    @Test
    fun `should return Invalid when nickname is blank string`() {
        val request = UpdatePetRequest(
            name = "Buddy",
            species = "DOG",
            breed = "Golden Retriever",
            age = 3,
            birthdate = null,
            weight = null,
            nickname = "",
        )

        val result = request.validate("pet-123", "user-123")

        assertTrue(result is Validated.Invalid)
        assertEquals("Pet nickname cannot be blank", result.error)
    }

    @Test
    fun `should accept zero age as valid`() {
        val request = UpdatePetRequest(
            name = "Puppy",
            species = "DOG",
            breed = "Mixed",
            age = 0,
            birthdate = null,
            weight = null,
            nickname = null,
        )

        val result = request.validate("pet-123", "user-123")

        assertTrue(result is Validated.Valid)
        val command = result.value
        assertEquals(0, command.age)
    }

    @Test
    fun `should accept null breed as valid`() {
        val request = UpdatePetRequest(
            name = "Mittens",
            species = "CAT",
            breed = null,
            age = 2,
            birthdate = null,
            weight = null,
            nickname = null,
        )

        val result = request.validate("pet-123", "user-123")

        assertTrue(result is Validated.Valid)
        val command = result.value
        assertEquals(null, command.breed)
    }

    @Test
    fun `should accept null nickname as valid`() {
        val request = UpdatePetRequest(
            name = "Buddy",
            species = "DOG",
            breed = "Golden Retriever",
            age = 3,
            birthdate = null,
            weight = null,
            nickname = null,
        )

        val result = request.validate("pet-123", "user-123")

        assertTrue(result is Validated.Valid)
        val command = result.value
        assertEquals(null, command.nickname)
    }

    @Test
    fun `should accept null weight as valid`() {
        val request = UpdatePetRequest(
            name = "Buddy",
            species = "DOG",
            breed = "Golden Retriever",
            age = 3,
            birthdate = null,
            weight = null,
            nickname = null,
        )

        val result = request.validate("pet-123", "user-123")

        assertTrue(result is Validated.Valid)
        val command = result.value
        assertEquals(null, command.weight)
    }

    @Test
    fun `should accept null birthdate as valid`() {
        val request = UpdatePetRequest(
            name = "Buddy",
            species = "DOG",
            breed = "Golden Retriever",
            age = 3,
            birthdate = null,
            weight = null,
            nickname = null,
        )

        val result = request.validate("pet-123", "user-123")

        assertTrue(result is Validated.Valid)
        val command = result.value
        assertEquals(null, command.birthdate)
    }

    @Test
    fun `should handle species case insensitively`() {
        val requestLowercase = UpdatePetRequest(
            name = "Buddy",
            species = "dog",
            breed = "Golden Retriever",
            age = 3,
            birthdate = null,
            weight = null,
            nickname = null,
        )

        val resultLowercase = requestLowercase.validate("pet-123", "user-123")
        assertTrue(resultLowercase is Validated.Valid)
        assertEquals(Species.DOG, resultLowercase.value.species)

        val requestMixed = UpdatePetRequest(
            name = "Mittens",
            species = "CaT",
            breed = "Persian",
            age = 2,
            birthdate = null,
            weight = null,
            nickname = null,
        )

        val resultMixed = requestMixed.validate("pet-456", "user-456")
        assertTrue(resultMixed is Validated.Valid)
        assertEquals(Species.CAT, resultMixed.value.species)
    }

    @Test
    fun `should accept positive weight as valid`() {
        val request = UpdatePetRequest(
            name = "Buddy",
            species = "DOG",
            breed = "Golden Retriever",
            age = 3,
            birthdate = null,
            weight = BigDecimal("0.1"),
            nickname = null,
        )

        val result = request.validate("pet-123", "user-123")

        assertTrue(result is Validated.Valid)
        val command = result.value
        assertEquals(BigDecimal("0.1"), command.weight)
    }
}
