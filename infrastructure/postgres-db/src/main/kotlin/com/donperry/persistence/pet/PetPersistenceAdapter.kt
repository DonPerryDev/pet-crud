package com.donperry.persistence.pet

import com.donperry.model.pet.Pet
import com.donperry.model.pet.gateway.PetPersistenceGateway
import com.donperry.persistence.pet.mapper.PetMapper
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono

@Service
class PetPersistenceAdapter(
    private val petRepository: PetRepository
) : PetPersistenceGateway {

    override fun save(pet: Pet): Mono<Pet> {
        return petRepository.save(PetMapper.toEntity(pet))
            .map { PetMapper.toModel(it) }
    }

    override fun countByOwner(userId: String): Mono<Long> {
        return petRepository.countByOwner(userId)
    }
}