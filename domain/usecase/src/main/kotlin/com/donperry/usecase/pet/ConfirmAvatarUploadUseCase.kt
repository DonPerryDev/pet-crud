package com.donperry.usecase.pet

import com.donperry.model.exception.PetNotFoundException
import com.donperry.model.exception.PhotoNotFoundException
import com.donperry.model.exception.UnauthorizedException
import com.donperry.model.exception.ValidationException
import com.donperry.model.pet.ConfirmAvatarUploadCommand
import com.donperry.model.pet.Pet
import com.donperry.model.pet.gateway.PetPersistenceGateway
import com.donperry.model.pet.gateway.PhotoStorageGateway
import reactor.core.publisher.Mono
import java.util.logging.Logger

class ConfirmAvatarUploadUseCase(
    private val petPersistenceGateway: PetPersistenceGateway,
    private val photoStorageGateway: PhotoStorageGateway
) {
    companion object {
        private val logger: Logger = Logger.getLogger(ConfirmAvatarUploadUseCase::class.java.name)
        private val ALLOWED_CONTENT_TYPES = setOf("image/jpeg", "image/png")
    }

    fun execute(command: ConfirmAvatarUploadCommand): Mono<Pet> {
        logger.info("[${command.userId}] Confirming avatar upload for pet ${command.petId}")

        return Mono.fromCallable {
            validateContentType(command.contentType)
            photoStorageGateway.buildPhotoKey(command.userId, command.petId, command.contentType)
        }
        .flatMap { photoKey ->
            petPersistenceGateway.findById(command.petId)
                .switchIfEmpty(Mono.error(PetNotFoundException(command.petId)))
                .map { pet -> pet to photoKey }
        }
        .flatMap { (pet, photoKey) ->
            if (pet.owner != command.userId) {
                logger.warning("[${command.userId}] Unauthorized access attempt to pet ${command.petId}")
                Mono.error(UnauthorizedException("User ${command.userId} is not the owner of pet ${command.petId}"))
            } else {
                logger.fine("[${command.userId}] Pet ownership verified for ${command.petId}")
                photoStorageGateway.verifyPhotoExists(photoKey)
                    .flatMap { exists ->
                        if (!exists) {
                            Mono.error(PhotoNotFoundException(photoKey))
                        } else {
                            Mono.just(pet to photoKey)
                        }
                    }
            }
        }
        .flatMap { (pet, photoKey) ->
            val photoUrl = photoStorageGateway.buildPhotoUrl(photoKey)
            logger.info("[${command.userId}] Photo verified, updating pet with URL: $photoUrl")
            petPersistenceGateway.save(pet.copy(photoUrl = photoUrl))
        }
        .doOnNext { pet ->
            logger.info("[${command.userId}] Avatar upload confirmed successfully for pet ${command.petId}")
        }
        .doOnError { error ->
            logger.warning("[${command.userId}] Failed to confirm avatar upload: ${error.message}")
        }
    }

    private fun validateContentType(contentType: String) {
        when {
            contentType !in ALLOWED_CONTENT_TYPES -> throw ValidationException(
                "Invalid content type: $contentType. Must be one of: ${ALLOWED_CONTENT_TYPES.joinToString()}"
            )
        }
    }
}
