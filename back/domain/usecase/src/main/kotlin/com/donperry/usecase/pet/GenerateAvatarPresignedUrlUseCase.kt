package com.donperry.usecase.pet

import com.donperry.model.exception.PetNotFoundException
import com.donperry.model.exception.UnauthorizedException
import com.donperry.model.exception.ValidationException
import com.donperry.model.pet.GeneratePresignedUrlCommand
import com.donperry.model.pet.PresignedUploadUrl
import com.donperry.model.pet.gateway.PetPersistenceGateway
import com.donperry.model.pet.gateway.PhotoStorageGateway
import reactor.core.publisher.Mono
import java.util.logging.Logger

class GenerateAvatarPresignedUrlUseCase(
    private val petPersistenceGateway: PetPersistenceGateway,
    private val photoStorageGateway: PhotoStorageGateway
) {
    companion object {
        private val logger: Logger = Logger.getLogger(GenerateAvatarPresignedUrlUseCase::class.java.name)
        private const val PRESIGNED_URL_EXPIRATION_MINUTES = 15
        private val ALLOWED_CONTENT_TYPES = setOf("image/jpeg", "image/png")
    }

    fun execute(command: GeneratePresignedUrlCommand): Mono<PresignedUploadUrl> {
        logger.info("[${command.userId}] Generating presigned URL for pet ${command.petId}")

        return Mono.fromCallable {
            validateContentType(command.contentType)
        }
        .then(Mono.defer {
            petPersistenceGateway.findById(command.petId)
                .switchIfEmpty(Mono.error(PetNotFoundException(command.petId)))
        })
        .flatMap { pet ->
            if (pet.owner != command.userId) {
                logger.warning("[${command.userId}] Unauthorized access attempt to pet ${command.petId}")
                Mono.error(UnauthorizedException("User ${command.userId} is not the owner of pet ${command.petId}"))
            } else {
                logger.fine("[${command.userId}] Pet ownership verified for ${command.petId}")
                photoStorageGateway.generatePresignedUrl(
                    command.userId,
                    command.petId,
                    command.contentType,
                    PRESIGNED_URL_EXPIRATION_MINUTES
                )
            }
        }
        .doOnNext { presignedUrl ->
            logger.info("[${command.userId}] Presigned URL generated successfully for pet ${command.petId}")
        }
        .doOnError { error ->
            logger.warning("[${command.userId}] Failed to generate presigned URL: ${error.message}")
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
