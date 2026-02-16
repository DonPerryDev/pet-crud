package com.donperry.usecase.pet

import com.donperry.model.exception.PetNotFoundException
import com.donperry.model.exception.UnauthorizedException
import com.donperry.model.exception.ValidationException
import com.donperry.model.pet.Pet
import com.donperry.model.pet.Species
import com.donperry.model.pet.UpdatePetCommand
import com.donperry.model.pet.gateway.PetPersistenceGateway
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import reactor.core.publisher.Mono
import reactor.test.StepVerifier
import java.math.BigDecimal
import java.time.LocalDate
import kotlin.test.assertEquals

@ExtendWith(MockitoExtension::class)
class UpdatePetUseCaseTest {

    @Mock
    private lateinit var petPersistenceGateway: PetPersistenceGateway

    @InjectMocks
    private lateinit var updatePetUseCase: UpdatePetUseCase

    @Test
    fun `should update pet when user is owner`() {
        val userId = "user-123"
        val petId = "pet-123"
        val existingPet = Pet(
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
            photoUrl = null
        )

        val updatedPet = Pet(
            id = petId,
            name = "Buddy Updated",
            species = Species.CAT,
            breed = "Persian",
            age = 4,
            birthdate = LocalDate.of(2020, 5, 10),
            weight = BigDecimal("30.0"),
            nickname = "Buddy Bear",
            owner = userId,
            registrationDate = LocalDate.of(2023, 6, 1),
            photoUrl = "https://example.com/photo.jpg"
        )

        val command = UpdatePetCommand(
            petId = petId,
            userId = userId,
            name = "Buddy Updated",
            species = Species.CAT,
            breed = "Persian",
            age = 4,
            birthdate = LocalDate.of(2020, 5, 10),
            weight = BigDecimal("30.0"),
            nickname = "Buddy Bear",
            photoUrl = "https://example.com/photo.jpg"
        )

        whenever(petPersistenceGateway.findById(petId)).thenReturn(Mono.just(existingPet))
        whenever(petPersistenceGateway.update(any())).thenReturn(Mono.just(updatedPet))

        StepVerifier.create(updatePetUseCase.execute(command))
            .expectNext(updatedPet)
            .verifyComplete()

        verify(petPersistenceGateway).findById(petId)
        verify(petPersistenceGateway).update(any())
    }

    @Test
    fun `should throw UnauthorizedException when user is not owner`() {
        val userId = "user-123"
        val ownerId = "user-456"
        val petId = "pet-123"
        val existingPet = Pet(
            id = petId,
            name = "Buddy",
            species = Species.DOG,
            breed = "Golden Retriever",
            age = 3,
            owner = ownerId,
            registrationDate = LocalDate.of(2023, 6, 1),
            photoUrl = null
        )

        val command = UpdatePetCommand(
            petId = petId,
            userId = userId,
            name = "Buddy Updated",
            species = Species.DOG,
            breed = "Golden Retriever",
            age = 4,
            birthdate = null,
            weight = null,
            nickname = null,
            photoUrl = null
        )

        whenever(petPersistenceGateway.findById(petId)).thenReturn(Mono.just(existingPet))

        StepVerifier.create(updatePetUseCase.execute(command))
            .expectErrorMatches { throwable ->
                throwable is UnauthorizedException &&
                throwable.message == "User $userId is not authorized to update pet $petId"
            }
            .verify()

        verify(petPersistenceGateway).findById(petId)
        verify(petPersistenceGateway, never()).update(any())
    }

    @Test
    fun `should throw PetNotFoundException when pet does not exist`() {
        val userId = "user-123"
        val petId = "pet-999"

        val command = UpdatePetCommand(
            petId = petId,
            userId = userId,
            name = "Buddy",
            species = Species.DOG,
            breed = "Golden Retriever",
            age = 3,
            birthdate = null,
            weight = null,
            nickname = null,
            photoUrl = null
        )

        whenever(petPersistenceGateway.findById(petId)).thenReturn(Mono.empty())

        StepVerifier.create(updatePetUseCase.execute(command))
            .expectErrorMatches { throwable ->
                throwable is PetNotFoundException &&
                throwable.message == "Pet with id $petId not found"
            }
            .verify()

        verify(petPersistenceGateway).findById(petId)
        verify(petPersistenceGateway, never()).update(any())
    }

    @Test
    fun `should preserve read-only fields id owner and registrationDate`() {
        val userId = "user-123"
        val petId = "pet-123"
        val originalRegistrationDate = LocalDate.of(2023, 6, 1)
        val existingPet = Pet(
            id = petId,
            name = "Buddy",
            species = Species.DOG,
            breed = "Golden Retriever",
            age = 3,
            owner = userId,
            registrationDate = originalRegistrationDate,
            photoUrl = null
        )

        val updatedPet = Pet(
            id = petId,
            name = "Updated Name",
            species = Species.CAT,
            breed = "Persian",
            age = 4,
            owner = userId,
            registrationDate = originalRegistrationDate,
            photoUrl = null
        )

        val command = UpdatePetCommand(
            petId = petId,
            userId = userId,
            name = "Updated Name",
            species = Species.CAT,
            breed = "Persian",
            age = 4,
            birthdate = null,
            weight = null,
            nickname = null,
            photoUrl = null
        )

        whenever(petPersistenceGateway.findById(petId)).thenReturn(Mono.just(existingPet))
        whenever(petPersistenceGateway.update(any())).thenReturn(Mono.just(updatedPet))

        StepVerifier.create(updatePetUseCase.execute(command))
            .expectNext(updatedPet)
            .verifyComplete()

        val petCaptor = argumentCaptor<Pet>()
        verify(petPersistenceGateway).update(petCaptor.capture())

        val capturedPet = petCaptor.firstValue
        assertEquals(petId, capturedPet.id)
        assertEquals(userId, capturedPet.owner)
        assertEquals(originalRegistrationDate, capturedPet.registrationDate)
    }

    @Test
    fun `should throw ValidationException when name is blank`() {
        val command = UpdatePetCommand(
            petId = "pet-123",
            userId = "user-123",
            name = "",
            species = Species.DOG,
            breed = "Golden Retriever",
            age = 3,
            birthdate = null,
            weight = null,
            nickname = null,
            photoUrl = null
        )

        whenever(petPersistenceGateway.findById(any())).thenReturn(Mono.empty())

        StepVerifier.create(updatePetUseCase.execute(command))
            .expectErrorMatches { throwable ->
                throwable is ValidationException &&
                throwable.message == "Pet name cannot be blank"
            }
            .verify()

        verify(petPersistenceGateway, never()).update(any())
    }

    @Test
    fun `should throw ValidationException when userId is blank`() {
        val command = UpdatePetCommand(
            petId = "pet-123",
            userId = "",
            name = "Buddy",
            species = Species.DOG,
            breed = "Golden Retriever",
            age = 3,
            birthdate = null,
            weight = null,
            nickname = null,
            photoUrl = null
        )

        whenever(petPersistenceGateway.findById(any())).thenReturn(Mono.empty())

        StepVerifier.create(updatePetUseCase.execute(command))
            .expectErrorMatches { throwable ->
                throwable is ValidationException &&
                throwable.message == "User ID cannot be blank"
            }
            .verify()

        verify(petPersistenceGateway, never()).update(any())
    }

    @Test
    fun `should throw ValidationException when age is negative`() {
        val command = UpdatePetCommand(
            petId = "pet-123",
            userId = "user-123",
            name = "Buddy",
            species = Species.DOG,
            breed = "Golden Retriever",
            age = -1,
            birthdate = null,
            weight = null,
            nickname = null,
            photoUrl = null
        )

        whenever(petPersistenceGateway.findById(any())).thenReturn(Mono.empty())

        StepVerifier.create(updatePetUseCase.execute(command))
            .expectErrorMatches { throwable ->
                throwable is ValidationException &&
                throwable.message == "Pet age must be zero or greater"
            }
            .verify()

        verify(petPersistenceGateway, never()).update(any())
    }

    @Test
    fun `should throw ValidationException when weight is zero`() {
        val command = UpdatePetCommand(
            petId = "pet-123",
            userId = "user-123",
            name = "Buddy",
            species = Species.DOG,
            breed = "Golden Retriever",
            age = 3,
            birthdate = null,
            weight = BigDecimal.ZERO,
            nickname = null,
            photoUrl = null
        )

        whenever(petPersistenceGateway.findById(any())).thenReturn(Mono.empty())

        StepVerifier.create(updatePetUseCase.execute(command))
            .expectErrorMatches { throwable ->
                throwable is ValidationException &&
                throwable.message == "Pet weight must be greater than zero"
            }
            .verify()

        verify(petPersistenceGateway, never()).update(any())
    }

    @Test
    fun `should throw ValidationException when weight is negative`() {
        val command = UpdatePetCommand(
            petId = "pet-123",
            userId = "user-123",
            name = "Buddy",
            species = Species.DOG,
            breed = "Golden Retriever",
            age = 3,
            birthdate = null,
            weight = BigDecimal("-5.0"),
            nickname = null,
            photoUrl = null
        )

        whenever(petPersistenceGateway.findById(any())).thenReturn(Mono.empty())

        StepVerifier.create(updatePetUseCase.execute(command))
            .expectErrorMatches { throwable ->
                throwable is ValidationException &&
                throwable.message == "Pet weight must be greater than zero"
            }
            .verify()

        verify(petPersistenceGateway, never()).update(any())
    }

    @Test
    fun `should throw ValidationException when birthdate is in future`() {
        val futureBirthdate = LocalDate.now().plusDays(1)

        val command = UpdatePetCommand(
            petId = "pet-123",
            userId = "user-123",
            name = "Buddy",
            species = Species.DOG,
            breed = "Golden Retriever",
            age = 3,
            birthdate = futureBirthdate,
            weight = null,
            nickname = null,
            photoUrl = null
        )

        whenever(petPersistenceGateway.findById(any())).thenReturn(Mono.empty())

        StepVerifier.create(updatePetUseCase.execute(command))
            .expectErrorMatches { throwable ->
                throwable is ValidationException &&
                throwable.message == "Pet birthdate cannot be in the future"
            }
            .verify()

        verify(petPersistenceGateway, never()).update(any())
    }

    @Test
    fun `should throw ValidationException with multiple errors when multiple fields invalid`() {
        val command = UpdatePetCommand(
            petId = "pet-123",
            userId = "",
            name = "",
            species = Species.DOG,
            breed = "Golden Retriever",
            age = -1,
            birthdate = null,
            weight = null,
            nickname = null,
            photoUrl = null
        )

        whenever(petPersistenceGateway.findById(any())).thenReturn(Mono.empty())

        StepVerifier.create(updatePetUseCase.execute(command))
            .expectErrorMatches { throwable ->
                throwable is ValidationException &&
                throwable.message!!.contains("User ID cannot be blank") &&
                throwable.message!!.contains("Pet name cannot be blank") &&
                throwable.message!!.contains("Pet age must be zero or greater")
            }
            .verify()

        verify(petPersistenceGateway, never()).update(any())
    }

    @Test
    fun `should accept zero age as valid`() {
        val userId = "user-123"
        val petId = "pet-123"
        val existingPet = Pet(
            id = petId,
            name = "Puppy",
            species = Species.DOG,
            breed = "Mixed",
            age = 0,
            owner = userId,
            registrationDate = LocalDate.now(),
            photoUrl = null
        )

        val updatedPet = Pet(
            id = petId,
            name = "Baby Puppy",
            species = Species.DOG,
            breed = "Mixed",
            age = 0,
            owner = userId,
            registrationDate = LocalDate.now(),
            photoUrl = null
        )

        val command = UpdatePetCommand(
            petId = petId,
            userId = userId,
            name = "Baby Puppy",
            species = Species.DOG,
            breed = "Mixed",
            age = 0,
            birthdate = null,
            weight = null,
            nickname = null,
            photoUrl = null
        )

        whenever(petPersistenceGateway.findById(petId)).thenReturn(Mono.just(existingPet))
        whenever(petPersistenceGateway.update(any())).thenReturn(Mono.just(updatedPet))

        StepVerifier.create(updatePetUseCase.execute(command))
            .expectNext(updatedPet)
            .verifyComplete()

        val petCaptor = argumentCaptor<Pet>()
        verify(petPersistenceGateway).update(petCaptor.capture())
        assertEquals(0, petCaptor.firstValue.age)
    }

    @Test
    fun `should update all editable fields when valid`() {
        val userId = "user-123"
        val petId = "pet-123"
        val existingPet = Pet(
            id = petId,
            name = "Buddy",
            species = Species.DOG,
            breed = "Golden Retriever",
            age = 3,
            birthdate = null,
            weight = null,
            nickname = null,
            owner = userId,
            registrationDate = LocalDate.of(2023, 6, 1),
            photoUrl = null
        )

        val command = UpdatePetCommand(
            petId = petId,
            userId = userId,
            name = "Max",
            species = Species.CAT,
            breed = "Persian",
            age = 5,
            birthdate = LocalDate.of(2019, 3, 15),
            weight = BigDecimal("12.5"),
            nickname = "Maxie",
            photoUrl = "https://example.com/photo.jpg"
        )

        val updatedPet = Pet(
            id = petId,
            name = "Max",
            species = Species.CAT,
            breed = "Persian",
            age = 5,
            birthdate = LocalDate.of(2019, 3, 15),
            weight = BigDecimal("12.5"),
            nickname = "Maxie",
            owner = userId,
            registrationDate = LocalDate.of(2023, 6, 1),
            photoUrl = "https://example.com/photo.jpg"
        )

        whenever(petPersistenceGateway.findById(petId)).thenReturn(Mono.just(existingPet))
        whenever(petPersistenceGateway.update(any())).thenReturn(Mono.just(updatedPet))

        StepVerifier.create(updatePetUseCase.execute(command))
            .expectNext(updatedPet)
            .verifyComplete()

        val petCaptor = argumentCaptor<Pet>()
        verify(petPersistenceGateway).update(petCaptor.capture())

        val capturedPet = petCaptor.firstValue
        assertEquals("Max", capturedPet.name)
        assertEquals(Species.CAT, capturedPet.species)
        assertEquals("Persian", capturedPet.breed)
        assertEquals(5, capturedPet.age)
        assertEquals(LocalDate.of(2019, 3, 15), capturedPet.birthdate)
        assertEquals(BigDecimal("12.5"), capturedPet.weight)
        assertEquals("Maxie", capturedPet.nickname)
        assertEquals("https://example.com/photo.jpg", capturedPet.photoUrl)
    }

    @Test
    fun `should propagate error when findById fails`() {
        val userId = "user-123"
        val petId = "pet-123"
        val findError = RuntimeException("Database connection failed")

        val command = UpdatePetCommand(
            petId = petId,
            userId = userId,
            name = "Buddy",
            species = Species.DOG,
            breed = "Golden Retriever",
            age = 3,
            birthdate = null,
            weight = null,
            nickname = null,
            photoUrl = null
        )

        whenever(petPersistenceGateway.findById(petId)).thenReturn(Mono.error(findError))

        StepVerifier.create(updatePetUseCase.execute(command))
            .expectErrorMatches { throwable ->
                throwable is RuntimeException && throwable.message == "Database connection failed"
            }
            .verify()

        verify(petPersistenceGateway).findById(petId)
        verify(petPersistenceGateway, never()).update(any())
    }

    @Test
    fun `should propagate error when update fails`() {
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
            photoUrl = null
        )

        val updateError = RuntimeException("Database constraint violation")

        val command = UpdatePetCommand(
            petId = petId,
            userId = userId,
            name = "Buddy Updated",
            species = Species.DOG,
            breed = "Golden Retriever",
            age = 4,
            birthdate = null,
            weight = null,
            nickname = null,
            photoUrl = null
        )

        whenever(petPersistenceGateway.findById(petId)).thenReturn(Mono.just(existingPet))
        whenever(petPersistenceGateway.update(any())).thenReturn(Mono.error(updateError))

        StepVerifier.create(updatePetUseCase.execute(command))
            .expectErrorMatches { throwable ->
                throwable is RuntimeException && throwable.message == "Database constraint violation"
            }
            .verify()

        verify(petPersistenceGateway).findById(petId)
        verify(petPersistenceGateway).update(any())
    }
}
