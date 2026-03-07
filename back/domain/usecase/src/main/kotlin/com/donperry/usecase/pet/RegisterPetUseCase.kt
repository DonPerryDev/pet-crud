package com.donperry.usecase.pet

import com.donperry.model.exception.PetLimitExceededException
import com.donperry.model.exception.ValidationException
import com.donperry.model.pet.Pet
import com.donperry.model.pet.RegisterPetCommand
import com.donperry.model.pet.gateway.PetPersistenceGateway
import reactor.core.publisher.Mono
import java.math.BigDecimal
import java.time.LocalDate
import java.util.logging.Logger

class RegisterPetUseCase(
    private val petPersistenceGateway: PetPersistenceGateway
) {
    companion object {
        private val logger: Logger = Logger.getLogger(RegisterPetUseCase::class.java.name)
        private const val MAX_PETS_PER_USER = 10L
    }

    fun execute(command: RegisterPetCommand): Mono<Pet> {
        logger.info("[${command.userId}] Starting pet registration process for pet: ${command.name}")

        return Mono.fromCallable {
            validateInputs(command.userId, command.name, command.age, command.birthdate, command.weight)
        }
        .then(Mono.defer { checkPetLimit(command.userId) })
        .then(Mono.defer {
            val pet = Pet(
                name = command.name,
                species = command.species,
                breed = command.breed,
                age = command.age,
                birthdate = command.birthdate,
                weight = command.weight,
                nickname = command.nickname,
                owner = command.userId,
                registrationDate = LocalDate.now(),
                photoUrl = null
            )
            petPersistenceGateway.save(pet)
        })
        .doOnNext { pet ->
            logger.info("[${pet.id}] Pet registration completed successfully")
        }
        .doOnError { error ->
            logger.warning("[${command.userId}] Pet registration failed: ${error.message}")
            logger.fine("[${command.userId}] Error details: ${error.javaClass.name}")
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

    private fun checkPetLimit(userId: String): Mono<Void> {
        return petPersistenceGateway.countByOwner(userId)
            .flatMap { count ->
                if (count >= MAX_PETS_PER_USER) {
                    logger.warning("[$userId] Pet limit exceeded: $count pets already registered")
                    Mono.error(PetLimitExceededException(userId))
                } else {
                    logger.fine("[$userId] Pet count check passed: $count/$MAX_PETS_PER_USER")
                    Mono.empty()
                }
            }
    }
}
