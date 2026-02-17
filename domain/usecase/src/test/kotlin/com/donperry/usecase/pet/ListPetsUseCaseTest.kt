package com.donperry.usecase.pet

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
import reactor.core.publisher.Flux
import reactor.test.StepVerifier
import java.time.LocalDate

@ExtendWith(MockitoExtension::class)
class ListPetsUseCaseTest {

    @Mock
    private lateinit var petPersistenceGateway: PetPersistenceGateway

    @InjectMocks
    private lateinit var listPetsUseCase: ListPetsUseCase

    @Test
    fun `should return all active pets when user has multiple pets`() {
        val userId = "user-123"
        val pet1 = Pet(
            id = "pet-1",
            name = "Buddy",
            species = Species.DOG,
            breed = "Golden Retriever",
            age = 3,
            owner = userId,
            registrationDate = LocalDate.of(2023, 1, 15),
            photoUrl = "https://example.com/buddy.jpg"
        )
        val pet2 = Pet(
            id = "pet-2",
            name = "Mittens",
            species = Species.CAT,
            breed = "Persian",
            age = 2,
            owner = userId,
            registrationDate = LocalDate.of(2023, 6, 20),
            photoUrl = null
        )
        val pet3 = Pet(
            id = "pet-3",
            name = "Charlie",
            species = Species.DOG,
            breed = "Beagle",
            age = 5,
            owner = userId,
            registrationDate = LocalDate.of(2022, 11, 10),
            photoUrl = "https://example.com/charlie.jpg"
        )

        whenever(petPersistenceGateway.findAllByOwner(userId))
            .thenReturn(Flux.just(pet1, pet2, pet3))

        StepVerifier.create(listPetsUseCase.execute(userId))
            .expectNext(pet1)
            .expectNext(pet2)
            .expectNext(pet3)
            .verifyComplete()

        verify(petPersistenceGateway).findAllByOwner(userId)
    }

    @Test
    fun `should return single pet when user has one pet`() {
        val userId = "user-456"
        val pet = Pet(
            id = "pet-solo",
            name = "Rex",
            species = Species.DOG,
            breed = null,
            age = 1,
            owner = userId,
            registrationDate = LocalDate.now(),
            photoUrl = null
        )

        whenever(petPersistenceGateway.findAllByOwner(userId))
            .thenReturn(Flux.just(pet))

        StepVerifier.create(listPetsUseCase.execute(userId))
            .expectNext(pet)
            .verifyComplete()

        verify(petPersistenceGateway).findAllByOwner(userId)
    }

    @Test
    fun `should return empty flux when user has no pets`() {
        val userId = "user-no-pets"

        whenever(petPersistenceGateway.findAllByOwner(userId))
            .thenReturn(Flux.empty())

        StepVerifier.create(listPetsUseCase.execute(userId))
            .verifyComplete()

        verify(petPersistenceGateway).findAllByOwner(userId)
    }

    @Test
    fun `should include pets with null optional fields`() {
        val userId = "user-789"
        val pet = Pet(
            id = "pet-minimal",
            name = "Fluffy",
            species = Species.CAT,
            breed = null,
            age = 0,
            birthdate = null,
            weight = null,
            nickname = null,
            owner = userId,
            registrationDate = LocalDate.now(),
            photoUrl = null
        )

        whenever(petPersistenceGateway.findAllByOwner(userId))
            .thenReturn(Flux.just(pet))

        StepVerifier.create(listPetsUseCase.execute(userId))
            .expectNext(pet)
            .verifyComplete()

        verify(petPersistenceGateway).findAllByOwner(userId)
    }

    @Test
    fun `should propagate error when gateway fails`() {
        val userId = "user-error"
        val error = RuntimeException("Database connection failed")

        whenever(petPersistenceGateway.findAllByOwner(userId))
            .thenReturn(Flux.error(error))

        StepVerifier.create(listPetsUseCase.execute(userId))
            .expectErrorMatches { throwable ->
                throwable is RuntimeException && throwable.message == "Database connection failed"
            }
            .verify()

        verify(petPersistenceGateway).findAllByOwner(userId)
    }

    @Test
    fun `should handle gateway returning error after some elements`() {
        val userId = "user-partial"
        val pet1 = Pet(
            id = "pet-1",
            name = "Buddy",
            species = Species.DOG,
            breed = "Golden Retriever",
            age = 3,
            owner = userId,
            registrationDate = LocalDate.now(),
            photoUrl = null
        )
        val error = RuntimeException("Stream interrupted")

        whenever(petPersistenceGateway.findAllByOwner(userId))
            .thenReturn(Flux.just(pet1).concatWith(Flux.error(error)))

        StepVerifier.create(listPetsUseCase.execute(userId))
            .expectNext(pet1)
            .expectErrorMatches { throwable ->
                throwable is RuntimeException && throwable.message == "Stream interrupted"
            }
            .verify()

        verify(petPersistenceGateway).findAllByOwner(userId)
    }

    @Test
    fun `should return pets with different species`() {
        val userId = "user-variety"
        val dog1 = Pet(
            id = "pet-dog-1",
            name = "Max",
            species = Species.DOG,
            breed = "Labrador",
            age = 4,
            owner = userId,
            registrationDate = LocalDate.now(),
            photoUrl = null
        )
        val cat1 = Pet(
            id = "pet-cat-1",
            name = "Whiskers",
            species = Species.CAT,
            breed = "Siamese",
            age = 2,
            owner = userId,
            registrationDate = LocalDate.now(),
            photoUrl = null
        )
        val dog2 = Pet(
            id = "pet-dog-2",
            name = "Buddy",
            species = Species.DOG,
            breed = "Beagle",
            age = 3,
            owner = userId,
            registrationDate = LocalDate.now(),
            photoUrl = null
        )
        val cat2 = Pet(
            id = "pet-cat-2",
            name = "Mittens",
            species = Species.CAT,
            breed = "Persian",
            age = 1,
            owner = userId,
            registrationDate = LocalDate.now(),
            photoUrl = null
        )

        whenever(petPersistenceGateway.findAllByOwner(userId))
            .thenReturn(Flux.just(dog1, cat1, dog2, cat2))

        StepVerifier.create(listPetsUseCase.execute(userId))
            .expectNext(dog1)
            .expectNext(cat1)
            .expectNext(dog2)
            .expectNext(cat2)
            .verifyComplete()

        verify(petPersistenceGateway).findAllByOwner(userId)
    }

    @Test
    fun `should handle user with maximum allowed pets`() {
        val userId = "user-max"
        val pets = (1..10).map { index ->
            Pet(
                id = "pet-$index",
                name = "Pet $index",
                species = Species.DOG,
                breed = "Mixed",
                age = index,
                owner = userId,
                registrationDate = LocalDate.now(),
                photoUrl = null
            )
        }

        whenever(petPersistenceGateway.findAllByOwner(userId))
            .thenReturn(Flux.fromIterable(pets))

        StepVerifier.create(listPetsUseCase.execute(userId))
            .expectNextSequence(pets)
            .verifyComplete()

        verify(petPersistenceGateway).findAllByOwner(userId)
    }

    @Test
    fun `should return pets ordered by registration date descending`() {
        val userId = "user-ordered"
        val oldestPet = Pet(
            id = "pet-old",
            name = "Senior",
            species = Species.DOG,
            breed = "Poodle",
            age = 10,
            owner = userId,
            registrationDate = LocalDate.of(2020, 1, 1),
            photoUrl = null
        )
        val newestPet = Pet(
            id = "pet-new",
            name = "Puppy",
            species = Species.DOG,
            breed = "Beagle",
            age = 1,
            owner = userId,
            registrationDate = LocalDate.of(2024, 12, 31),
            photoUrl = null
        )
        val middlePet = Pet(
            id = "pet-mid",
            name = "Middle",
            species = Species.CAT,
            breed = "Tabby",
            age = 5,
            owner = userId,
            registrationDate = LocalDate.of(2022, 6, 15),
            photoUrl = null
        )

        whenever(petPersistenceGateway.findAllByOwner(userId))
            .thenReturn(Flux.just(newestPet, middlePet, oldestPet))

        StepVerifier.create(listPetsUseCase.execute(userId))
            .expectNext(newestPet)
            .expectNext(middlePet)
            .expectNext(oldestPet)
            .verifyComplete()

        verify(petPersistenceGateway).findAllByOwner(userId)
    }
}
