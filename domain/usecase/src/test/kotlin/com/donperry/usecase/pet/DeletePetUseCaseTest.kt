package com.donperry.usecase.pet

import com.donperry.model.exception.PetNotFoundException
import com.donperry.model.pet.DeletePetCommand
import com.donperry.model.pet.Pet
import com.donperry.model.pet.Species
import com.donperry.model.pet.gateway.PetPersistenceGateway
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import reactor.core.publisher.Mono
import reactor.test.StepVerifier
import java.time.LocalDate
import java.time.LocalDateTime

@ExtendWith(MockitoExtension::class)
class DeletePetUseCaseTest {

    @Mock
    private lateinit var petPersistenceGateway: PetPersistenceGateway

    @InjectMocks
    private lateinit var deletePetUseCase: DeletePetUseCase

    @Test
    fun `should soft-delete pet when user is owner and pet is active`() {
        val userId = "user-123"
        val petId = "pet-123"
        val existingPet = Pet(
            id = petId,
            name = "Buddy",
            species = Species.DOG,
            breed = "Golden Retriever",
            age = 3,
            owner = userId,
            registrationDate = LocalDate.of(2023, 6, 1),
            photoUrl = null,
            deletedAt = null
        )

        val command = DeletePetCommand(
            petId = petId,
            userId = userId
        )

        whenever(petPersistenceGateway.findById(petId)).thenReturn(Mono.just(existingPet))
        whenever(petPersistenceGateway.softDelete(petId)).thenReturn(Mono.empty())

        StepVerifier.create(deletePetUseCase.execute(command))
            .verifyComplete()

        verify(petPersistenceGateway).findById(petId)
        verify(petPersistenceGateway).softDelete(petId)
    }

    @Test
    fun `should be idempotent when pet is already deleted`() {
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

        val command = DeletePetCommand(
            petId = petId,
            userId = userId
        )

        whenever(petPersistenceGateway.findById(petId)).thenReturn(Mono.just(deletedPet))

        StepVerifier.create(deletePetUseCase.execute(command))
            .verifyComplete()

        verify(petPersistenceGateway).findById(petId)
        verify(petPersistenceGateway, never()).softDelete(any())
    }

    @Test
    fun `should throw PetNotFoundException when pet does not exist`() {
        val userId = "user-123"
        val petId = "pet-999"

        val command = DeletePetCommand(
            petId = petId,
            userId = userId
        )

        whenever(petPersistenceGateway.findById(petId)).thenReturn(Mono.empty())

        StepVerifier.create(deletePetUseCase.execute(command))
            .expectErrorMatches { throwable ->
                throwable is PetNotFoundException &&
                throwable.message == "Pet with id $petId not found"
            }
            .verify()

        verify(petPersistenceGateway).findById(petId)
        verify(petPersistenceGateway, never()).softDelete(any())
    }

    @Test
    fun `should throw PetNotFoundException when user is not owner`() {
        val ownerId = "user-456"
        val requestingUserId = "user-123"
        val petId = "pet-123"
        val existingPet = Pet(
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

        val command = DeletePetCommand(
            petId = petId,
            userId = requestingUserId
        )

        whenever(petPersistenceGateway.findById(petId)).thenReturn(Mono.just(existingPet))

        StepVerifier.create(deletePetUseCase.execute(command))
            .expectErrorMatches { throwable ->
                throwable is PetNotFoundException &&
                throwable.message == "Pet with id $petId not found"
            }
            .verify()

        verify(petPersistenceGateway).findById(petId)
        verify(petPersistenceGateway, never()).softDelete(any())
    }

    @Test
    fun `should propagate error when findById fails`() {
        val userId = "user-123"
        val petId = "pet-123"
        val findError = RuntimeException("Database connection failed")

        val command = DeletePetCommand(
            petId = petId,
            userId = userId
        )

        whenever(petPersistenceGateway.findById(petId)).thenReturn(Mono.error(findError))

        StepVerifier.create(deletePetUseCase.execute(command))
            .expectErrorMatches { throwable ->
                throwable is RuntimeException && throwable.message == "Database connection failed"
            }
            .verify()

        verify(petPersistenceGateway).findById(petId)
        verify(petPersistenceGateway, never()).softDelete(any())
    }

    @Test
    fun `should propagate error when softDelete fails`() {
        val userId = "user-123"
        val petId = "pet-123"
        val existingPet = Pet(
            id = petId,
            name = "Buddy",
            species = Species.DOG,
            breed = "Golden Retriever",
            age = 3,
            owner = userId,
            registrationDate = LocalDate.of(2023, 6, 1),
            photoUrl = null,
            deletedAt = null
        )

        val deleteError = RuntimeException("Database update failed")

        val command = DeletePetCommand(
            petId = petId,
            userId = userId
        )

        whenever(petPersistenceGateway.findById(petId)).thenReturn(Mono.just(existingPet))
        whenever(petPersistenceGateway.softDelete(petId)).thenReturn(Mono.error(deleteError))

        StepVerifier.create(deletePetUseCase.execute(command))
            .expectErrorMatches { throwable ->
                throwable is RuntimeException && throwable.message == "Database update failed"
            }
            .verify()

        verify(petPersistenceGateway).findById(petId)
        verify(petPersistenceGateway).softDelete(petId)
    }

    @Test
    fun `should delete pet with all optional fields populated`() {
        val userId = "user-123"
        val petId = "pet-123"
        val existingPet = Pet(
            id = petId,
            name = "Buddy",
            species = Species.DOG,
            breed = "Golden Retriever",
            age = 3,
            birthdate = LocalDate.of(2021, 1, 15),
            weight = java.math.BigDecimal("25.5"),
            nickname = "Bud",
            owner = userId,
            registrationDate = LocalDate.of(2023, 6, 1),
            photoUrl = "https://example.com/photo.jpg",
            deletedAt = null
        )

        val command = DeletePetCommand(
            petId = petId,
            userId = userId
        )

        whenever(petPersistenceGateway.findById(petId)).thenReturn(Mono.just(existingPet))
        whenever(petPersistenceGateway.softDelete(petId)).thenReturn(Mono.empty())

        StepVerifier.create(deletePetUseCase.execute(command))
            .verifyComplete()

        verify(petPersistenceGateway).findById(petId)
        verify(petPersistenceGateway).softDelete(petId)
    }

    @Test
    fun `should delete pet with minimal fields`() {
        val userId = "user-123"
        val petId = "pet-123"
        val existingPet = Pet(
            id = petId,
            name = "Buddy",
            species = Species.DOG,
            breed = null,
            age = 0,
            birthdate = null,
            weight = null,
            nickname = null,
            owner = userId,
            registrationDate = LocalDate.now(),
            photoUrl = null,
            deletedAt = null
        )

        val command = DeletePetCommand(
            petId = petId,
            userId = userId
        )

        whenever(petPersistenceGateway.findById(petId)).thenReturn(Mono.just(existingPet))
        whenever(petPersistenceGateway.softDelete(petId)).thenReturn(Mono.empty())

        StepVerifier.create(deletePetUseCase.execute(command))
            .verifyComplete()

        verify(petPersistenceGateway).findById(petId)
        verify(petPersistenceGateway).softDelete(petId)
    }

    @Test
    fun `should delete cat species`() {
        val userId = "user-123"
        val petId = "pet-123"
        val existingPet = Pet(
            id = petId,
            name = "Whiskers",
            species = Species.CAT,
            breed = "Persian",
            age = 2,
            owner = userId,
            registrationDate = LocalDate.of(2024, 1, 1),
            photoUrl = null,
            deletedAt = null
        )

        val command = DeletePetCommand(
            petId = petId,
            userId = userId
        )

        whenever(petPersistenceGateway.findById(petId)).thenReturn(Mono.just(existingPet))
        whenever(petPersistenceGateway.softDelete(petId)).thenReturn(Mono.empty())

        StepVerifier.create(deletePetUseCase.execute(command))
            .verifyComplete()

        verify(petPersistenceGateway).findById(petId)
        verify(petPersistenceGateway).softDelete(petId)
    }

    @Test
    fun `should delete pet with photo url`() {
        val userId = "user-123"
        val petId = "pet-123"
        val existingPet = Pet(
            id = petId,
            name = "Buddy",
            species = Species.DOG,
            breed = "Labrador",
            age = 1,
            owner = userId,
            registrationDate = LocalDate.of(2024, 1, 1),
            photoUrl = "https://example.com/photo.jpg",
            deletedAt = null
        )

        val command = DeletePetCommand(
            petId = petId,
            userId = userId
        )

        whenever(petPersistenceGateway.findById(petId)).thenReturn(Mono.just(existingPet))
        whenever(petPersistenceGateway.softDelete(petId)).thenReturn(Mono.empty())

        StepVerifier.create(deletePetUseCase.execute(command))
            .verifyComplete()

        verify(petPersistenceGateway).findById(petId)
        verify(petPersistenceGateway).softDelete(petId)
    }
}
