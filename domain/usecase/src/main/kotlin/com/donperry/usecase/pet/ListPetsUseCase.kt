package com.donperry.usecase.pet

import com.donperry.model.pet.Pet
import com.donperry.model.pet.gateway.PetPersistenceGateway
import reactor.core.publisher.Flux
import java.util.logging.Logger

class ListPetsUseCase(
    private val petPersistenceGateway: PetPersistenceGateway
) {
    companion object {
        private val logger: Logger = Logger.getLogger(ListPetsUseCase::class.java.name)
    }

    fun execute(userId: String): Flux<Pet> {
        logger.info("[$userId] Listing all active pets")
        return petPersistenceGateway.findAllByOwner(userId)
            .doOnComplete {
                logger.info("[$userId] Completed listing all active pets")
            }
            .doOnError { error ->
                logger.warning("[$userId] Failed to list active pets: ${error.message}")
            }
    }
}
