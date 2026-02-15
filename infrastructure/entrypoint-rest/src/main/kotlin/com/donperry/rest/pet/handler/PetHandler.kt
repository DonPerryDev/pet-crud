package com.donperry.rest.pet.handler

import com.donperry.model.exception.PetLimitExceededException
import com.donperry.model.exception.PetNotFoundException
import com.donperry.model.exception.PhotoNotFoundException
import com.donperry.model.exception.PhotoSizeExceededException
import com.donperry.model.exception.PhotoUploadException
import com.donperry.model.exception.UnauthorizedException
import com.donperry.model.exception.ValidationException
import com.donperry.model.pet.ConfirmAvatarUploadCommand
import com.donperry.model.pet.GeneratePresignedUrlCommand
import com.donperry.model.pet.RegisterPetCommand
import com.donperry.model.pet.Species
import com.donperry.rest.common.dto.ErrorResponse
import com.donperry.rest.pet.dto.ConfirmAvatarUploadRequest
import com.donperry.rest.pet.dto.GeneratePresignedUrlRequest
import com.donperry.rest.pet.dto.PetResponse
import com.donperry.rest.pet.dto.PresignedUrlResponse
import com.donperry.rest.pet.dto.RegisterPetRequest
import com.donperry.usecase.pet.ConfirmAvatarUploadUseCase
import com.donperry.usecase.pet.GenerateAvatarPresignedUrlUseCase
import com.donperry.usecase.pet.RegisterPetUseCase
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import reactor.core.publisher.Mono
import java.time.Instant
import java.util.logging.Logger

@Component
class PetHandler(
    private val registerPetUseCase: RegisterPetUseCase,
    private val generateAvatarPresignedUrlUseCase: GenerateAvatarPresignedUrlUseCase,
    private val confirmAvatarUploadUseCase: ConfirmAvatarUploadUseCase
) {
    companion object {
        private val logger: Logger = Logger.getLogger(PetHandler::class.java.name)
    }

    fun registerPet(request: ServerRequest): Mono<ServerResponse> {
        logger.fine("Received pet registration request")

        return Mono.defer {
            val userId = extractUserId(request)
            logger.info("[$userId] Processing pet registration")

            request.bodyToMono(RegisterPetRequest::class.java)
            .flatMap { petRequest ->
                if (petRequest.name.isBlank()) {
                    return@flatMap Mono.error<ServerResponse>(
                        ValidationException("Pet name cannot be blank")
                    )
                }
                if (petRequest.species.isBlank()) {
                    return@flatMap Mono.error<ServerResponse>(
                        ValidationException("Pet species cannot be blank")
                    )
                }
                petRequest.breed?.let { breed ->
                    if (breed.isBlank()) {
                        return@flatMap Mono.error<ServerResponse>(
                            ValidationException("Pet breed cannot be blank")
                        )
                    }
                }
                petRequest.nickname?.let { nickname ->
                    if (nickname.isBlank()) {
                        return@flatMap Mono.error<ServerResponse>(
                            ValidationException("Pet nickname cannot be blank")
                        )
                    }
                }

                val species = Species.entries.find {
                    it.name.equals(petRequest.species, ignoreCase = true)
                } ?: return@flatMap Mono.error<ServerResponse>(
                    ValidationException("Invalid species: ${petRequest.species}. Must be one of: ${Species.entries.joinToString()}")
                )

                val command = RegisterPetCommand(
                    userId = userId,
                    name = petRequest.name,
                    species = species,
                    breed = petRequest.breed,
                    age = petRequest.age,
                    birthdate = petRequest.birthdate,
                    weight = petRequest.weight,
                    nickname = petRequest.nickname
                )

                registerPetUseCase.execute(command)
                    .flatMap { pet -> buildCreatedResponse(pet) }
            }
        }
            .onErrorResume { throwable ->
                handleError(throwable)
            }
    }

    fun generatePresignedUrl(request: ServerRequest): Mono<ServerResponse> {
        val petId = request.pathVariable("petId")
        logger.fine("Received presigned URL generation request for pet: $petId")

        return Mono.defer {
            val userId = extractUserId(request)
            logger.info("[$userId] Generating presigned URL for pet $petId")

            request.bodyToMono(GeneratePresignedUrlRequest::class.java)
            .flatMap { urlRequest ->
                if (urlRequest.contentType.isBlank()) {
                    return@flatMap Mono.error<ServerResponse>(
                        ValidationException("Content type cannot be blank")
                    )
                }

                val command = GeneratePresignedUrlCommand(
                    userId = userId,
                    petId = petId,
                    contentType = urlRequest.contentType
                )

                generateAvatarPresignedUrlUseCase.execute(command)
                    .flatMap { presignedUrl ->
                        val response = PresignedUrlResponse(
                            uploadUrl = presignedUrl.uploadUrl,
                            key = presignedUrl.key,
                            expiresAt = presignedUrl.expiresAt.toString()
                        )
                        ServerResponse.ok()
                            .contentType(MediaType.APPLICATION_JSON)
                            .bodyValue(response)
                    }
            }
        }
            .onErrorResume { throwable ->
                handleError(throwable)
            }
    }

    fun confirmAvatarUpload(request: ServerRequest): Mono<ServerResponse> {
        val petId = request.pathVariable("petId")
        logger.fine("Received avatar upload confirmation request for pet: $petId")

        return Mono.defer {
            val userId = extractUserId(request)
            logger.info("[$userId] Confirming avatar upload for pet $petId")

            request.bodyToMono(ConfirmAvatarUploadRequest::class.java)
            .flatMap { confirmRequest ->
                if (confirmRequest.photoKey.isBlank()) {
                    return@flatMap Mono.error<ServerResponse>(
                        ValidationException("Photo key cannot be blank")
                    )
                }

                val command = ConfirmAvatarUploadCommand(
                    userId = userId,
                    petId = petId,
                    photoKey = confirmRequest.photoKey
                )

                confirmAvatarUploadUseCase.execute(command)
                    .flatMap { pet -> buildOkResponse(pet) }
            }
        }
            .onErrorResume { throwable ->
                handleError(throwable)
            }
    }

    private fun extractUserId(request: ServerRequest): String =
        request.attribute("userId")
            .map { it as String }
            .orElseThrow { UnauthorizedException("No authentication found") }

    private fun toPetResponse(pet: com.donperry.model.pet.Pet): PetResponse =
        PetResponse(
            id = pet.id!!,
            name = pet.name,
            species = pet.species.name,
            breed = pet.breed,
            age = pet.age,
            birthdate = pet.birthdate,
            weight = pet.weight,
            nickname = pet.nickname,
            owner = pet.owner,
            registrationDate = pet.registrationDate,
            photoUrl = pet.photoUrl
        )

    private fun buildCreatedResponse(pet: com.donperry.model.pet.Pet): Mono<ServerResponse> =
        ServerResponse.status(HttpStatus.CREATED)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(toPetResponse(pet))

    private fun buildOkResponse(pet: com.donperry.model.pet.Pet): Mono<ServerResponse> =
        ServerResponse.ok()
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(toPetResponse(pet))

    private fun handleError(throwable: Throwable): Mono<ServerResponse> {
        logger.warning("Error during pet operation: ${throwable.message}")
        logger.fine("Error details: ${throwable.javaClass.name} - ${throwable.message}")

        return when (throwable) {
            is ValidationException -> buildErrorResponse(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", throwable)
            is PetLimitExceededException -> buildErrorResponse(HttpStatus.CONFLICT, "PET_LIMIT_EXCEEDED", throwable)
            is PhotoSizeExceededException -> buildErrorResponse(HttpStatus.PAYLOAD_TOO_LARGE, "PHOTO_SIZE_EXCEEDED", throwable)
            is UnauthorizedException -> buildErrorResponse(HttpStatus.UNAUTHORIZED, "UNAUTHORIZED", throwable)
            is PhotoUploadException -> buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "PHOTO_UPLOAD_FAILED", throwable)
            is PetNotFoundException -> buildErrorResponse(HttpStatus.NOT_FOUND, "PET_NOT_FOUND", throwable)
            is PhotoNotFoundException -> buildErrorResponse(HttpStatus.NOT_FOUND, "PHOTO_NOT_FOUND", throwable)
            else -> buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_ERROR", throwable)
        }
    }

    private fun buildErrorResponse(status: HttpStatus, error: String, throwable: Throwable): Mono<ServerResponse> {
        val errorResponse = ErrorResponse(
            error = error,
            message = throwable.message ?: status.reasonPhrase,
            timestamp = Instant.now().toString()
        )
        return ServerResponse.status(status)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(errorResponse)
    }
}
