package com.donperry.persistence.pet

import com.donperry.model.pet.Pet
import com.donperry.model.pet.gateway.PetPersistenceGateway
import com.donperry.persistence.pet.mapper.PetMapper
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import java.util.UUID
import java.util.logging.Logger

@Service
class PetPersistenceAdapter(
    private val petRepository: PetRepository
) : PetPersistenceGateway {

    companion object {
        private val logger: Logger = Logger.getLogger(PetPersistenceAdapter::class.java.name)
    }

    override fun save(pet: Pet): Mono<Pet> {
        logger.fine("[${pet.id ?: "new"}] Saving pet to database: name=${pet.name}")
        return petRepository.save(PetMapper.toEntity(pet))
            .map { PetMapper.toModel(it) }
            .doOnNext { savedPet ->
                logger.info("[${savedPet.id}] Pet saved to database successfully")
            }
            .doOnError { error ->
                logger.warning("[${pet.id ?: "new"}] Failed to save pet to database: ${error.message}")
            }
    }

    override fun countByOwner(userId: String): Mono<Long> {
        logger.fine("[$userId] Counting pets for owner")
        return petRepository.countByOwner(userId)
            .doOnNext { count ->
                logger.fine("[$userId] Pet count retrieved: $count")
            }
            .doOnError { error ->
                logger.warning("[$userId] Failed to count pets: ${error.message}")
            }
    }

    override fun findById(petId: String): Mono<Pet> {
        logger.fine("[$petId] Finding pet by ID")
        return petRepository.findById(UUID.fromString(petId))
            .map { PetMapper.toModel(it) }
            .doOnNext { pet ->
                logger.fine("[$petId] Pet found: ${pet.name}")
            }
            .doOnError { error ->
                logger.warning("[$petId] Failed to find pet: ${error.message}")
            }
    }

    override fun update(pet: Pet): Mono<Pet> {
        logger.fine("[${pet.id}] Updating pet in database: name=${pet.name}")
        return petRepository.save(PetMapper.toEntity(pet))
            .map { PetMapper.toModel(it) }
            .doOnNext { updatedPet ->
                logger.info("[${updatedPet.id}] Pet updated in database successfully")
            }
            .doOnError { error ->
                logger.warning("[${pet.id}] Failed to update pet in database: ${error.message}")
            }
    }
}