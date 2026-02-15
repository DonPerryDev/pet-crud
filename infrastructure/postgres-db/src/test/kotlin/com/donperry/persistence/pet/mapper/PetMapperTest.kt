package com.donperry.persistence.pet.mapper

import com.donperry.model.pet.Pet
import com.donperry.model.pet.Species
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
        val birthdate = LocalDate.of(2020, 5, 15)
        val weight = java.math.BigDecimal("30.5")

        val petModel = Pet(
            id = petId.toString(),
            name = "Buddy",
            species = Species.DOG,
            breed = "Golden Retriever",
            age = 3,
            birthdate = birthdate,
            weight = weight,
            nickname = "Bud",
            owner = "John Doe",
            registrationDate = registrationDate,
            photoUrl = "https://s3.amazonaws.com/pets/buddy.jpg"
        )

        // When
        val result = PetMapper.toEntity(petModel)

        // Then
        assertEquals(petId, result.id)
        assertEquals("Buddy", result.name)
        assertEquals("DOG", result.species)
        assertEquals("Golden Retriever", result.breed)
        assertEquals(3, result.age)
        assertEquals(birthdate, result.birthdate)
        assertEquals(weight, result.weight)
        assertEquals("Bud", result.nickname)
        assertEquals("John Doe", result.owner)
        assertEquals(registrationDate, result.registrationDate)
        assertEquals("https://s3.amazonaws.com/pets/buddy.jpg", result.photoUrl)
    }

    @Test
    fun `toEntity should handle Pet model with null id`() {
        // Given
        val petModel = Pet(
            id = null,
            name = "Mittens",
            species = Species.CAT,
            breed = "Persian",
            age = 2,
            birthdate = null,
            weight = null,
            nickname = null,
            owner = "Jane Smith",
            registrationDate = LocalDate.now(),
            photoUrl = null
        )

        // When
        val result = PetMapper.toEntity(petModel)

        // Then
        assertNull(result.id)
        assertEquals("Mittens", result.name)
        assertEquals("CAT", result.species)
        assertEquals("Persian", result.breed)
        assertEquals(2, result.age)
        assertNull(result.birthdate)
        assertNull(result.weight)
        assertNull(result.nickname)
        assertEquals("Jane Smith", result.owner)
        assertNull(result.photoUrl)
    }

    @Test
    fun `toEntity should handle Pet model with null breed`() {
        // Given
        val petModel = Pet(
            id = UUID.randomUUID().toString(),
            name = "Rex",
            species = Species.DOG,
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
        assertEquals("DOG", result.species)
        assertEquals(5, result.age)
        assertEquals("Bob Wilson", result.owner)
    }

    @Test
    fun `toEntity should handle Pet model with empty string breed`() {
        // Given
        val petModel = Pet(
            id = UUID.randomUUID().toString(),
            name = "Fluffy",
            species = Species.CAT,
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
        val birthdate = LocalDate.of(2019, 3, 10)
        val weight = java.math.BigDecimal("25.5")

        val petData = PetData(
            id = petId,
            name = "Charlie",
            species = "DOG",
            breed = "Labrador",
            age = 4,
            birthdate = birthdate,
            weight = weight,
            nickname = "Chuck",
            owner = "Mike Johnson",
            registrationDate = registrationDate,
            photoUrl = "https://s3.amazonaws.com/pets/charlie.jpg"
        )

        // When
        val result = PetMapper.toModel(petData)

        // Then
        assertEquals(petId.toString(), result.id)
        assertEquals("Charlie", result.name)
        assertEquals(Species.DOG, result.species)
        assertEquals("Labrador", result.breed)
        assertEquals(4, result.age)
        assertEquals(birthdate, result.birthdate)
        assertEquals(weight, result.weight)
        assertEquals("Chuck", result.nickname)
        assertEquals("Mike Johnson", result.owner)
        assertEquals(registrationDate, result.registrationDate)
        assertEquals("https://s3.amazonaws.com/pets/charlie.jpg", result.photoUrl)
    }

    @Test
    fun `toModel should handle PetData entity with null breed`() {
        // Given
        val petId = UUID.randomUUID()
        val petData = PetData(
            id = petId,
            name = "Whiskers",
            species = "CAT",
            breed = null,
            age = 3,
            birthdate = null,
            weight = null,
            nickname = null,
            owner = "Sarah Connor",
            registrationDate = LocalDate.now(),
            photoUrl = null
        )

        // When
        val result = PetMapper.toModel(petData)

        // Then
        assertEquals(petId.toString(), result.id)
        assertNull(result.breed)
        assertNull(result.birthdate)
        assertNull(result.weight)
        assertNull(result.nickname)
        assertNull(result.photoUrl)
        assertEquals("Whiskers", result.name)
        assertEquals(Species.CAT, result.species)
    }

    @Test
    fun `toModel should handle PetData entity with empty string breed`() {
        // Given
        val petId = UUID.randomUUID()
        val petData = PetData(
            id = petId,
            name = "Spot",
            species = "DOG",
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
        val birthdate = LocalDate.of(2017, 4, 20)
        val weight = java.math.BigDecimal("20.0")
        val originalPet = Pet(
            id = UUID.randomUUID().toString(),
            name = "Bidirectional",
            species = Species.DOG,
            breed = "Mixed",
            age = 6,
            birthdate = birthdate,
            weight = weight,
            nickname = "Bidi",
            owner = "Test Owner",
            registrationDate = LocalDate.of(2023, 10, 10),
            photoUrl = "https://s3.amazonaws.com/pets/bidi.jpg"
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
        assertEquals(originalPet.birthdate, convertedPet.birthdate)
        assertEquals(originalPet.weight, convertedPet.weight)
        assertEquals(originalPet.nickname, convertedPet.nickname)
        assertEquals(originalPet.owner, convertedPet.owner)
        assertEquals(originalPet.registrationDate, convertedPet.registrationDate)
        assertEquals(originalPet.photoUrl, convertedPet.photoUrl)
    }

    @Test
    fun `toModel and toEntity should be bidirectional for Pet with null values`() {
        // Given
        val originalPet = Pet(
            id = null,
            name = "NullTest",
            species = Species.CAT,
            breed = null,
            age = 1,
            birthdate = null,
            weight = null,
            nickname = null,
            owner = "Null Owner",
            registrationDate = LocalDate.now(),
            photoUrl = null
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
        assertNull(convertedPet.birthdate)
        assertNull(convertedPet.weight)
        assertNull(convertedPet.nickname)
        assertNull(convertedPet.photoUrl)
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
            species = Species.CAT,
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
            species = "DOG",
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