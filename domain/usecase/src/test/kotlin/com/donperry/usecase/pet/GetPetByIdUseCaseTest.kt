package com.donperry.usecase.pet

import com.donperry.model.exception.PetNotFoundException
import com.donperry.model.exception.UnauthorizedException
import com.donperry.model.pet.Pet
import com.donperry.model.pet.Species
import com.donperry.model.pet.gateway.PetPersistenceGateway
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import reactor.core.publisher.Mono
import reactor.test.StepVerifier
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

@ExtendWith(MockitoExtension::class)
class GetPetByIdUseCaseTest {

    @Mock
    private lateinit var petPersistenceGateway: PetPersistenceGateway

    @InjectMocks
    private lateinit var getPetByIdUseCase: GetPetByIdUseCase

    @Test
    fun `should return pet when found and user is owner and pet is not deleted`() {
        val userId = "user-123"
        val petId = "pet-123"
        val expectedPet = Pet(
            id = petId,
            name = "Buddy",
            species = Species.DOG,
            breed = "Golden Retriever",
            age = 3,
            birthdate = LocalDate.of(2021, 1, 15),
            weight = BigDecimal("25.5"),
            nickname = "Bud",
            owner = userId,
            registrationDate = LocalDate.of(2023, 6, 1),
            photoUrl = "https://example.com/photo.jpg",
            deletedAt = null
        )

        whenever(petPersistenceGateway.findById(petId)).thenReturn(Mono.just(expectedPet))

        StepVerifier.create(getPetByIdUseCase.execute(petId, userId))
            .expectNext(expectedPet)
            .verifyComplete()

        verify(petPersistenceGateway).findById(petId)
    }

    @Test
    fun `should return pet with minimal fields when found and user is owner`() {
        val userId = "user-456"
        val petId = "pet-456"
        val expectedPet = Pet(
            id = petId,
            name = "Rex",
            species = Species.CAT,
            breed = null,
            age = 1,
            birthdate = null,
            weight = null,
            nickname = null,
            owner = userId,
            registrationDate = LocalDate.now(),
            photoUrl = null,
            deletedAt = null
        )

        whenever(petPersistenceGateway.findById(petId)).thenReturn(Mono.just(expectedPet))

        StepVerifier.create(getPetByIdUseCase.execute(petId, userId))
            .expectNext(expectedPet)
            .verifyComplete()

        verify(petPersistenceGateway).findById(petId)
    }

    @Test
    fun `should throw PetNotFoundException when pet does not exist`() {
        val userId = "user-123"
        val petId = "pet-999"

        whenever(petPersistenceGateway.findById(petId)).thenReturn(Mono.empty())

        StepVerifier.create(getPetByIdUseCase.execute(petId, userId))
            .expectErrorMatches { throwable ->
                throwable is PetNotFoundException &&
                throwable.message == "Pet with id $petId not found"
            }
            .verify()

        verify(petPersistenceGateway).findById(petId)
    }

    @Test
    fun `should throw PetNotFoundException when pet is soft-deleted`() {
        val userId = "user-123"
        val petId = "pet-123"
        val deletedPet = Pet(
            id = petId,
            name = "Buddy",
            species = Species.DOG,
            breed = "Golden Retriever",
            age = 3,
            owner = userId,
            registrationDate = LocalDate.of(2023, 6, 1),
            photoUrl = null,
            deletedAt = LocalDateTime.now()
        )

        whenever(petPersistenceGateway.findById(petId)).thenReturn(Mono.just(deletedPet))

        StepVerifier.create(getPetByIdUseCase.execute(petId, userId))
            .expectErrorMatches { throwable ->
                throwable is PetNotFoundException &&
                throwable.message == "Pet with id $petId not found"
            }
            .verify()

        verify(petPersistenceGateway).findById(petId)
    }

    @Test
    fun `should throw UnauthorizedException when user is not the owner`() {
        val ownerId = "user-456"
        val requestingUserId = "user-123"
        val petId = "pet-123"
        val pet = Pet(
            id = petId,
            name = "Buddy",
            species = Species.DOG,
            breed = "Golden Retriever",
            age = 3,
            owner = ownerId,
            registrationDate = LocalDate.of(2023, 6, 1),
            photoUrl = null,
            deletedAt = null
        )

        whenever(petPersistenceGateway.findById(petId)).thenReturn(Mono.just(pet))

        StepVerifier.create(getPetByIdUseCase.execute(petId, requestingUserId))
            .expectErrorMatches { throwable ->
                throwable is UnauthorizedException &&
                throwable.message == "Not authorized to view this pet"
            }
            .verify()

        verify(petPersistenceGateway).findById(petId)
    }

    @Test
    fun `should throw PetNotFoundException when pet is soft-deleted even if user is owner`() {
        val userId = "user-123"
        val petId = "pet-123"
        val deletedPet = Pet(
            id = petId,
            name = "Buddy",
            species = Species.DOG,
            breed = "Golden Retriever",
            age = 3,
            birthdate = LocalDate.of(2021, 1, 15),
            weight = BigDecimal("25.5"),
            nickname = "Bud",
            owner = userId,
            registrationDate = LocalDate.of(2023, 6, 1),
            photoUrl = "https://example.com/photo.jpg",
            deletedAt = LocalDateTime.of(2024, 1, 15, 10, 30)
        )

        whenever(petPersistenceGateway.findById(petId)).thenReturn(Mono.just(deletedPet))

        StepVerifier.create(getPetByIdUseCase.execute(petId, userId))
            .expectErrorMatches { throwable ->
                throwable is PetNotFoundException &&
                throwable.message == "Pet with id $petId not found"
            }
            .verify()

        verify(petPersistenceGateway).findById(petId)
    }

    @Test
    fun `should throw UnauthorizedException when pet is not deleted but user is not owner`() {
        val ownerId = "user-owner"
        val requestingUserId = "user-other"
        val petId = "pet-789"
        val pet = Pet(
            id = petId,
            name = "Whiskers",
            species = Species.CAT,
            breed = "Persian",
            age = 2,
            birthdate = LocalDate.of(2022, 3, 10),
            weight = BigDecimal("4.5"),
            nickname = "Whis",
            owner = ownerId,
            registrationDate = LocalDate.of(2023, 8, 20),
            photoUrl = "https://example.com/whiskers.jpg",
            deletedAt = null
        )

        whenever(petPersistenceGateway.findById(petId)).thenReturn(Mono.just(pet))

        StepVerifier.create(getPetByIdUseCase.execute(petId, requestingUserId))
            .expectErrorMatches { throwable ->
                throwable is UnauthorizedException &&
                throwable.message == "Not authorized to view this pet"
            }
            .verify()

        verify(petPersistenceGateway).findById(petId)
    }

    @Test
    fun `should propagate error when findById fails`() {
        val userId = "user-123"
        val petId = "pet-123"
        val findError = RuntimeException("Database connection failed")

        whenever(petPersistenceGateway.findById(petId)).thenReturn(Mono.error(findError))

        StepVerifier.create(getPetByIdUseCase.execute(petId, userId))
            .expectErrorMatches { throwable ->
                throwable is RuntimeException && throwable.message == "Database connection failed"
            }
            .verify()

        verify(petPersistenceGateway).findById(petId)
    }

    @Test
    fun `should return cat species when found and user is owner`() {
        val userId = "user-cat-lover"
        val petId = "pet-cat-1"
        val expectedPet = Pet(
            id = petId,
            name = "Mittens",
            species = Species.CAT,
            breed = "Siamese",
            age = 5,
            owner = userId,
            registrationDate = LocalDate.of(2022, 3, 15),
            photoUrl = null,
            deletedAt = null
        )

        whenever(petPersistenceGateway.findById(petId)).thenReturn(Mono.just(expectedPet))

        StepVerifier.create(getPetByIdUseCase.execute(petId, userId))
            .expectNext(expectedPet)
            .verifyComplete()

        verify(petPersistenceGateway).findById(petId)
    }

    @Test
    fun `should return dog species when found and user is owner`() {
        val userId = "user-dog-lover"
        val petId = "pet-dog-1"
        val expectedPet = Pet(
            id = petId,
            name = "Max",
            species = Species.DOG,
            breed = "Labrador",
            age = 4,
            owner = userId,
            registrationDate = LocalDate.of(2021, 11, 1),
            photoUrl = "https://example.com/max.jpg",
            deletedAt = null
        )

        whenever(petPersistenceGateway.findById(petId)).thenReturn(Mono.just(expectedPet))

        StepVerifier.create(getPetByIdUseCase.execute(petId, userId))
            .expectNext(expectedPet)
            .verifyComplete()

        verify(petPersistenceGateway).findById(petId)
    }

    @Test
    fun `should return pet with zero age when found and user is owner`() {
        val userId = "user-new-pet"
        val petId = "pet-puppy"
        val expectedPet = Pet(
            id = petId,
            name = "Puppy",
            species = Species.DOG,
            breed = "Mixed",
            age = 0,
            owner = userId,
            registrationDate = LocalDate.now(),
            photoUrl = null,
            deletedAt = null
        )

        whenever(petPersistenceGateway.findById(petId)).thenReturn(Mono.just(expectedPet))

        StepVerifier.create(getPetByIdUseCase.execute(petId, userId))
            .expectNext(expectedPet)
            .verifyComplete()

        verify(petPersistenceGateway).findById(petId)
    }
}
