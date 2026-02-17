package com.donperry.usecase.pet

import com.donperry.model.exception.PetNotFoundException
import com.donperry.model.exception.UnauthorizedException
import com.donperry.model.pet.Pet
import com.donperry.model.pet.gateway.PetPersistenceGateway
import reactor.core.publisher.Mono
import java.util.logging.Logger

class GetPetByIdUseCase(
    private val petPersistenceGateway: PetPersistenceGateway
) {
    companion object {
        private val logger: Logger = Logger.getLogger(GetPetByIdUseCase::class.java.name)
    }

    fun execute(petId: String, userId: String): Mono<Pet> {
        logger.info("[$userId] Fetching pet details for $petId")

        return petPersistenceGateway.findById(petId)
            .filter { it.deletedAt == null }
            .switchIfEmpty(Mono.defer {
                logger.warning("[$userId] Pet not found or deleted: $petId")
                Mono.error(PetNotFoundException(petId))
            })
            .filter { it.owner == userId }
            .switchIfEmpty(Mono.defer {
                logger.warning("[$userId] Unauthorized attempt to view pet $petId")
                Mono.error(UnauthorizedException("Not authorized to view this pet"))
            })
            .doOnNext { logger.info("[$userId] Successfully retrieved pet details for $petId") }
            .doOnError { error ->
                logger.warning("[$userId] Failed to fetch pet $petId: ${error.message}")
            }
    }
}
