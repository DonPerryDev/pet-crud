package com.donperry.usecase.pet

import com.donperry.model.exception.PetLimitExceededException
import com.donperry.model.exception.PhotoSizeExceededException
import com.donperry.model.exception.ValidationException
import com.donperry.model.pet.Pet
import com.donperry.model.pet.PhotoUploadData
import com.donperry.model.pet.RegisterPetCommand
import com.donperry.model.pet.gateway.PetPersistenceGateway
import com.donperry.model.pet.gateway.PhotoStorageGateway
import reactor.core.publisher.Mono
import java.math.BigDecimal
import java.time.LocalDate
import java.util.logging.Logger

class RegisterPetUseCase(
    private val petPersistenceGateway: PetPersistenceGateway,
    private val photoStorageGateway: PhotoStorageGateway
) {
    companion object {
        private val logger: Logger = Logger.getLogger(RegisterPetUseCase::class.java.name)
        private const val MAX_PETS_PER_USER = 10L
        private const val MAX_PHOTO_SIZE_BYTES = 5L * 1024 * 1024
    }

    fun execute(command: RegisterPetCommand): Mono<Pet> {
        logger.info("[${command.userId}] Starting pet registration process for pet: ${command.name}")

        return Mono.fromCallable {
            validateInputs(command.userId, command.name, command.age, command.birthdate, command.weight)
            validatePhoto(command.photo?.fileSize)
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
                registrationDate = LocalDate.now()
            )
            petPersistenceGateway.save(pet)
        })
        .flatMap { savedPet ->
            uploadPhotoIfPresent(savedPet, command.userId, command.photo)
        }
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

    private fun validatePhoto(photoSize: Long?) {
        when {
            photoSize != null && photoSize > MAX_PHOTO_SIZE_BYTES ->
                throw PhotoSizeExceededException(photoSize, MAX_PHOTO_SIZE_BYTES)
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

    private fun uploadPhotoIfPresent(pet: Pet, userId: String, photo: PhotoUploadData?): Mono<Pet> {
        if (photo == null) {
            return Mono.just(pet)
        }

        logger.info("[${pet.id}] Uploading photo: ${photo.fileName}")
        return photoStorageGateway.uploadPhoto(userId, pet.id!!, photo)
            .flatMap { photoUrl ->
                logger.info("[${pet.id}] Photo uploaded successfully")
                petPersistenceGateway.save(pet.copy(photoUrl = photoUrl))
            }
    }
}
