package com.donperry.rest.pet.handler

import com.donperry.model.exception.PetLimitExceededException
import com.donperry.model.exception.PhotoSizeExceededException
import com.donperry.model.exception.PhotoUploadException
import com.donperry.model.exception.UnauthorizedException
import com.donperry.model.exception.ValidationException
import com.donperry.model.pet.PhotoUploadData
import com.donperry.model.pet.RegisterPetCommand
import com.donperry.model.pet.Species
import com.donperry.rest.common.dto.ErrorResponse
import com.donperry.rest.pet.dto.PetResponse
import com.donperry.rest.pet.dto.RegisterPetRequest
import com.donperry.usecase.pet.RegisterPetUseCase
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.codec.multipart.FilePart
import org.springframework.http.codec.multipart.Part
import org.springframework.security.core.context.ReactiveSecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import reactor.core.publisher.Mono
import java.time.Instant
import java.util.logging.Logger

@Component
class PetHandler(
    private val registerPetUseCase: RegisterPetUseCase
) {
    companion object {
        private val logger: Logger = Logger.getLogger(PetHandler::class.java.name)
        private val objectMapper = jacksonObjectMapper().apply {
            findAndRegisterModules()
        }
    }

    fun registerPet(request: ServerRequest): Mono<ServerResponse> {
        logger.fine("Received pet registration request")

        return ReactiveSecurityContextHolder.getContext()
            .map { it.authentication.name }
            .switchIfEmpty(Mono.error(UnauthorizedException("No authentication found")))
            .flatMap { userId ->
                logger.info("[$userId] Processing pet registration")
                request.multipartData()
                    .flatMap { parts ->
                        val petPart = parts.getFirst("pet")
                        val photoPart = parts.getFirst("photo") as? FilePart

                        if (petPart == null) {
                            return@flatMap Mono.error<ServerResponse>(
                                ValidationException("Missing 'pet' part in multipart request")
                            )
                        }

                        parseAndRegister(userId, petPart, photoPart)
                    }
            }
            .onErrorResume { throwable ->
                handleError(throwable)
            }
    }

    private fun parseAndRegister(
        userId: String,
        petPart: Part,
        photoPart: FilePart?
    ): Mono<ServerResponse> {
        return petPart.content()
            .reduce(ByteArray(0)) { existing, dataBuffer ->
                val bytes = ByteArray(dataBuffer.readableByteCount())
                dataBuffer.read(bytes)
                existing + bytes
            }
            .flatMap { bytes ->
                val petRequest = objectMapper.readValue(bytes, RegisterPetRequest::class.java)

                val species = try {
                    Species.valueOf(petRequest.species.uppercase())
                } catch (e: IllegalArgumentException) {
                    return@flatMap Mono.error<ServerResponse>(
                        ValidationException("Invalid species: ${petRequest.species}. Must be one of: ${Species.entries.joinToString()}")
                    )
                }

                if (photoPart != null) {
                    buildCommandWithPhoto(userId, petRequest, species, photoPart)
                } else {
                    val command = RegisterPetCommand(
                        userId = userId,
                        name = petRequest.name,
                        species = species,
                        breed = petRequest.breed,
                        age = petRequest.age,
                        birthdate = petRequest.birthdate,
                        weight = petRequest.weight,
                        nickname = petRequest.nickname,
                        photo = null
                    )
                    registerPetUseCase.execute(command)
                        .flatMap { pet -> buildCreatedResponse(pet) }
                }
            }
    }

    private fun buildCommandWithPhoto(
        userId: String,
        petRequest: RegisterPetRequest,
        species: Species,
        photoPart: FilePart
    ): Mono<ServerResponse> {
        return photoPart.content()
            .reduce(ByteArray(0)) { existing, dataBuffer ->
                val bytes = ByteArray(dataBuffer.readableByteCount())
                dataBuffer.read(bytes)
                existing + bytes
            }
            .flatMap { photoBytes ->
                val photoSize = photoBytes.size.toLong()
                logger.fine("[$userId] Photo upload: ${photoPart.filename()}, size: $photoSize bytes")

                val command = RegisterPetCommand(
                    userId = userId,
                    name = petRequest.name,
                    species = species,
                    breed = petRequest.breed,
                    age = petRequest.age,
                    birthdate = petRequest.birthdate,
                    weight = petRequest.weight,
                    nickname = petRequest.nickname,
                    photo = PhotoUploadData(
                        fileName = photoPart.filename(),
                        contentType = photoPart.headers().contentType?.toString() ?: "application/octet-stream",
                        fileSize = photoSize,
                        fileBytes = photoBytes
                    )
                )

                registerPetUseCase.execute(command)
                    .flatMap { pet -> buildCreatedResponse(pet) }
            }
    }

    private fun buildCreatedResponse(pet: com.donperry.model.pet.Pet): Mono<ServerResponse> {
        val response = PetResponse(
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
        return ServerResponse.status(HttpStatus.CREATED)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(response)
    }

    private fun handleError(throwable: Throwable): Mono<ServerResponse> {
        logger.warning("Error during pet registration: ${throwable.message}")
        logger.fine("Error details: ${throwable.javaClass.name} - ${throwable.message}")

        return when (throwable) {
            is ValidationException -> buildErrorResponse(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", throwable)
            is PetLimitExceededException -> buildErrorResponse(HttpStatus.CONFLICT, "PET_LIMIT_EXCEEDED", throwable)
            is PhotoSizeExceededException -> buildErrorResponse(HttpStatus.PAYLOAD_TOO_LARGE, "PHOTO_SIZE_EXCEEDED", throwable)
            is UnauthorizedException -> buildErrorResponse(HttpStatus.UNAUTHORIZED, "UNAUTHORIZED", throwable)
            is PhotoUploadException -> buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "PHOTO_UPLOAD_FAILED", throwable)
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
