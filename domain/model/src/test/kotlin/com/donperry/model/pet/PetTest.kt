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
        val species = Species.DOG
        val breed = "Golden Retriever"
        val age = 3
        val birthdate = LocalDate.of(2020, 5, 15)
        val weight = java.math.BigDecimal("30.5")
        val nickname = "Bud"
        val owner = "John Doe"
        val registrationDate = LocalDate.of(2023, 12, 25)
        val photoUrl = "https://s3.amazonaws.com/pets/buddy.jpg"

        // When
        val pet = Pet(
            id = "pet-123",
            name = name,
            species = species,
            breed = breed,
            age = age,
            birthdate = birthdate,
            weight = weight,
            nickname = nickname,
            owner = owner,
            registrationDate = registrationDate,
            photoUrl = photoUrl
        )

        // Then
        assertEquals("pet-123", pet.id)
        assertEquals(name, pet.name)
        assertEquals(species, pet.species)
        assertEquals(breed, pet.breed)
        assertEquals(age, pet.age)
        assertEquals(birthdate, pet.birthdate)
        assertEquals(weight, pet.weight)
        assertEquals(nickname, pet.nickname)
        assertEquals(owner, pet.owner)
        assertEquals(registrationDate, pet.registrationDate)
        assertEquals(photoUrl, pet.photoUrl)
    }

    @Test
    fun `Pet should be created with null id`() {
        // When
        val pet = Pet(
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

        // Then
        assertNull(pet.id)
        assertEquals("Mittens", pet.name)
        assertNull(pet.birthdate)
        assertNull(pet.weight)
        assertNull(pet.nickname)
        assertNull(pet.photoUrl)
    }

    @Test
    fun `Pet should be created with null breed`() {
        // When
        val pet = Pet(
            id = "pet-456",
            name = "Rex",
            species = Species.DOG,
            breed = null,
            age = 5,
            owner = "Bob Wilson",
            registrationDate = LocalDate.now()
        )

        // Then
        assertNull(pet.breed)
        assertEquals("Rex", pet.name)
        assertEquals(Species.DOG, pet.species)
    }

    @Test
    fun `Pet should handle empty string breed`() {
        // When
        val pet = Pet(
            id = "pet-789",
            name = "Fluffy",
            species = Species.CAT,
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
            species = Species.DOG,
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
            species = Species.DOG,
            breed = "Golden Retriever",
            age = 3,
            owner = "John Doe",
            registrationDate = registrationDate
        )

        val pet2 = Pet(
            id = "pet-123",
            name = "Buddy",
            species = Species.DOG,
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
            species = Species.DOG,
            breed = "Golden Retriever",
            age = 3,
            owner = "John Doe",
            registrationDate = LocalDate.now()
        )

        val pet2 = Pet(
            id = "pet-456",
            name = "Mittens",
            species = Species.CAT,
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
            species = Species.DOG,
            breed = "Golden Retriever",
            age = 3,
            owner = "John Doe",
            registrationDate = registrationDate
        )

        val pet2 = Pet(
            id = "pet-456",
            name = "Buddy",
            species = Species.DOG,
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
            species = Species.DOG,
            breed = "Golden Retriever",
            age = 3,
            owner = "John Doe",
            registrationDate = LocalDate.of(2023, 11, 20)
        )

        // When
        val (id, name, species, breed, age, birthdate, weight, nickname, owner, registrationDate, photoUrl) = pet

        // Then
        assertEquals("pet-123", id)
        assertEquals("Buddy", name)
        assertEquals(Species.DOG, species)
        assertEquals("Golden Retriever", breed)
        assertEquals(3, age)
        assertNull(birthdate)
        assertNull(weight)
        assertNull(nickname)
        assertEquals("John Doe", owner)
        assertEquals(LocalDate.of(2023, 11, 20), registrationDate)
        assertNull(photoUrl)
    }

    @Test
    fun `Pet copy should work correctly`() {
        // Given
        val originalPet = Pet(
            id = "pet-123",
            name = "Buddy",
            species = Species.DOG,
            breed = "Golden Retriever",
            age = 3,
            birthdate = LocalDate.of(2020, 5, 15),
            weight = java.math.BigDecimal("30.0"),
            nickname = "Bud",
            owner = "John Doe",
            registrationDate = LocalDate.now(),
            photoUrl = "https://s3.amazonaws.com/pets/buddy.jpg"
        )

        // When
        val copiedPet = originalPet.copy(
            age = 4,
            owner = "Jane Doe",
            photoUrl = "https://s3.amazonaws.com/pets/buddy-new.jpg"
        )

        // Then
        assertEquals(originalPet.id, copiedPet.id)
        assertEquals(originalPet.name, copiedPet.name)
        assertEquals(originalPet.species, copiedPet.species)
        assertEquals(originalPet.breed, copiedPet.breed)
        assertEquals(4, copiedPet.age) // Changed
        assertEquals(originalPet.birthdate, copiedPet.birthdate)
        assertEquals(originalPet.weight, copiedPet.weight)
        assertEquals(originalPet.nickname, copiedPet.nickname)
        assertEquals("Jane Doe", copiedPet.owner) // Changed
        assertEquals(originalPet.registrationDate, copiedPet.registrationDate)
        assertEquals("https://s3.amazonaws.com/pets/buddy-new.jpg", copiedPet.photoUrl) // Changed
    }

    @Test
    fun `Pet toString should include all fields`() {
        // Given
        val pet = Pet(
            id = "pet-123",
            name = "Buddy",
            species = Species.DOG,
            breed = "Golden Retriever",
            age = 3,
            birthdate = LocalDate.of(2020, 5, 15),
            weight = java.math.BigDecimal("30.5"),
            nickname = "Bud",
            owner = "John Doe",
            registrationDate = LocalDate.of(2023, 12, 25),
            photoUrl = "https://s3.amazonaws.com/pets/buddy.jpg"
        )

        // When
        val toStringResult = pet.toString()

        // Then
        assert(toStringResult.contains("pet-123"))
        assert(toStringResult.contains("Buddy"))
        assert(toStringResult.contains("DOG"))
        assert(toStringResult.contains("Golden Retriever"))
        assert(toStringResult.contains("3"))
        assert(toStringResult.contains("2020-05-15"))
        assert(toStringResult.contains("30.5"))
        assert(toStringResult.contains("Bud"))
        assert(toStringResult.contains("John Doe"))
        assert(toStringResult.contains("2023-12-25"))
        assert(toStringResult.contains("https://s3.amazonaws.com/pets/buddy.jpg"))
    }

    @Test
    fun `Pet should handle Species DOG correctly`() {
        // When
        val pet = Pet(
            id = "pet-dog",
            name = "Rex",
            species = Species.DOG,
            breed = "German Shepherd",
            age = 5,
            birthdate = null,
            weight = null,
            nickname = null,
            owner = "Bob Wilson",
            registrationDate = LocalDate.now(),
            photoUrl = null
        )

        // Then
        assertEquals(Species.DOG, pet.species)
    }

    @Test
    fun `Pet should handle Species CAT correctly`() {
        // When
        val pet = Pet(
            id = "pet-cat",
            name = "Whiskers",
            species = Species.CAT,
            breed = "Siamese",
            age = 4,
            birthdate = null,
            weight = null,
            nickname = null,
            owner = "Alice Brown",
            registrationDate = LocalDate.now(),
            photoUrl = null
        )

        // Then
        assertEquals(Species.CAT, pet.species)
    }
}