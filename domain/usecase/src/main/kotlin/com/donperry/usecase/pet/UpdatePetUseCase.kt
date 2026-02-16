package com.donperry.usecase.pet

import com.donperry.model.exception.PetNotFoundException
import com.donperry.model.exception.UnauthorizedException
import com.donperry.model.exception.ValidationException
import com.donperry.model.pet.Pet
import com.donperry.model.pet.UpdatePetCommand
import com.donperry.model.pet.gateway.PetPersistenceGateway
import reactor.core.publisher.Mono
import java.math.BigDecimal
import java.time.LocalDate
import java.util.logging.Logger

class UpdatePetUseCase(
    private val petPersistenceGateway: PetPersistenceGateway
) {
    companion object {
        private val logger: Logger = Logger.getLogger(UpdatePetUseCase::class.java.name)
    }

    fun execute(command: UpdatePetCommand): Mono<Pet> {
        logger.info("[${command.petId}] Starting pet update process for user: ${command.userId}")

        return Mono.fromCallable {
            validateInputs(command.userId, command.name, command.age, command.birthdate, command.weight)
        }
        .then(petPersistenceGateway.findById(command.petId))
        .switchIfEmpty(Mono.error(PetNotFoundException(command.petId)))
        .doOnNext { logger.fine("[${command.petId}] Found existing pet, verifying ownership") }
        .filter { it.owner == command.userId }
        .switchIfEmpty(Mono.defer {
            logger.warning("[${command.petId}] Unauthorized update attempt by user: ${command.userId}")
            Mono.error(UnauthorizedException("User ${command.userId} is not authorized to update pet ${command.petId}"))
        })
        .map { existingPet ->
            Pet(
                id = existingPet.id,
                name = command.name,
                species = command.species,
                breed = command.breed,
                age = command.age,
                birthdate = command.birthdate,
                weight = command.weight,
                nickname = command.nickname,
                owner = existingPet.owner,
                registrationDate = existingPet.registrationDate,
                photoUrl = command.photoUrl
            )
        }
        .flatMap { updatedPet -> petPersistenceGateway.update(updatedPet) }
        .doOnNext { pet ->
            logger.info("[${pet.id}] Pet update completed successfully")
        }
        .doOnError { error ->
            logger.warning("[${command.petId}] Pet update failed: ${error.message}")
            logger.fine("[${command.petId}] Error details: ${error.javaClass.name}")
        }
    }

    private fun validateInputs(userId: String, name: String, age: Int, birthdate: LocalDate?, weight: BigDecimal?) {
        val errors = listOfNotNull(
            "User ID cannot be blank".takeIf { userId.isBlank() },
            "Pet name cannot be blank".takeIf { name.isBlank() },
            "Pet age must be zero or greater".takeIf { age < 0 },
            "Pet weight must be greater than zero".takeIf { weight != null && weight <= BigDecimal.ZERO },
            "Pet birthdate cannot be in the future".takeIf { birthdate != null && birthdate.isAfter(LocalDate.now()) },
        )
        if (errors.isNotEmpty()) {
            throw ValidationException(errors.joinToString("; "))
        }
    }
}
