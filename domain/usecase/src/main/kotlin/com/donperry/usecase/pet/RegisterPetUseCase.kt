package com.donperry.usecase.pet

import com.donperry.model.exception.PetLimitExceededException
import com.donperry.model.exception.PhotoSizeExceededException
import com.donperry.model.exception.ValidationException
import com.donperry.model.pet.Pet
import com.donperry.model.pet.Species
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

    fun execute(
        userId: String,
        name: String,
        species: Species,
        breed: String?,
        age: Int,
        birthdate: LocalDate?,
        weight: BigDecimal?,
        nickname: String?,
        photoFileName: String?,
        photoContentType: String?,
        photoBytes: ByteArray?,
        photoSize: Long?
    ): Mono<Pet> {
        logger.info("[$userId] Starting pet registration process for pet: $name")

        return Mono.fromCallable {
            validateInputs(userId, name, age, birthdate, weight)
            validatePhoto(photoSize)
        }
        .then(checkPetLimit(userId))
        .then(Mono.defer {
            val pet = Pet(
                name = name,
                species = species,
                breed = breed,
                age = age,
                birthdate = birthdate,
                weight = weight,
                nickname = nickname,
                owner = userId,
                registrationDate = LocalDate.now()
            )
            petPersistenceGateway.save(pet)
        })
        .flatMap { savedPet ->
            uploadPhotoIfPresent(savedPet, userId, photoFileName, photoContentType, photoBytes, photoSize)
        }
        .doOnNext { pet ->
            logger.info("[${pet.id}] Pet registration completed successfully")
        }
        .doOnError { error ->
            logger.warning("[$userId] Pet registration failed: ${error.message}")
            logger.fine("[$userId] Error details: ${error.javaClass.name}")
        }
    }

    private fun validateInputs(userId: String, name: String, age: Int, birthdate: LocalDate?, weight: BigDecimal?) {
        if (userId.isBlank()) {
            throw ValidationException("User ID cannot be blank")
        }
        if (name.isBlank()) {
            throw ValidationException("Pet name cannot be blank")
        }
        if (age < 0) {
            throw ValidationException("Pet age must be zero or greater")
        }
        if (weight != null && weight <= BigDecimal.ZERO) {
            throw ValidationException("Pet weight must be greater than zero")
        }
        if (birthdate != null && birthdate.isAfter(LocalDate.now())) {
            throw ValidationException("Pet birthdate cannot be in the future")
        }
    }

    private fun validatePhoto(photoSize: Long?) {
        if (photoSize != null && photoSize > MAX_PHOTO_SIZE_BYTES) {
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

    private fun uploadPhotoIfPresent(
        pet: Pet,
        userId: String,
        photoFileName: String?,
        photoContentType: String?,
        photoBytes: ByteArray?,
        photoSize: Long?
    ): Mono<Pet> {
        if (photoFileName == null || photoContentType == null || photoBytes == null || photoSize == null) {
            return Mono.just(pet)
        }

        logger.info("[${pet.id}] Uploading photo: $photoFileName")
        return photoStorageGateway.uploadPhoto(
            userId = userId,
            petId = pet.id!!,
            fileName = photoFileName,
            contentType = photoContentType,
            fileSize = photoSize,
            fileBytes = photoBytes
        )
        .flatMap { photoUrl ->
            logger.info("[${pet.id}] Photo uploaded successfully")
            petPersistenceGateway.save(pet.copy(photoUrl = photoUrl))
        }
    }
}
