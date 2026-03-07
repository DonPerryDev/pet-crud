package com.donperry.usecase.pet

import com.donperry.model.exception.PetNotFoundException
import com.donperry.model.pet.DeletePetCommand
import com.donperry.model.pet.gateway.PetPersistenceGateway
import reactor.core.publisher.Mono
import java.util.logging.Logger

class DeletePetUseCase(
    private val petPersistenceGateway: PetPersistenceGateway
) {
    companion object {
        private val logger: Logger = Logger.getLogger(DeletePetUseCase::class.java.name)
    }

    fun execute(command: DeletePetCommand): Mono<Void> {
        logger.info("[${command.petId}] Starting pet soft-delete process for user: ${command.userId}")

        return petPersistenceGateway.findById(command.petId)
            .switchIfEmpty(Mono.defer {
                logger.warning("[${command.petId}] Pet not found for deletion")
                Mono.error(PetNotFoundException(command.petId))
            })
            .doOnNext { logger.fine("[${command.petId}] Found existing pet, verifying ownership") }
            .filter { it.owner == command.userId }
            .switchIfEmpty(Mono.defer {
                logger.warning("[${command.petId}] Unauthorized delete attempt by user: ${command.userId}")
                Mono.error(PetNotFoundException(command.petId))
            })
            .filter { it.deletedAt == null }
            .switchIfEmpty(Mono.defer {
                logger.info("[${command.petId}] Pet already soft-deleted, operation is idempotent")
                Mono.empty()
            })
            .flatMap {
                logger.fine("[${command.petId}] Proceeding with soft-delete")
                petPersistenceGateway.softDelete(command.petId)
            }
            .doOnSuccess {
                logger.info("[${command.petId}] Pet soft-delete completed successfully")
            }
            .doOnError { error ->
                logger.warning("[${command.petId}] Pet soft-delete failed: ${error.message}")
                logger.fine("[${command.petId}] Error details: ${error.javaClass.name}")
            }
    }
}
