package com.donperry.model.pet.gateway

import com.donperry.model.pet.Pet
import reactor.core.publisher.Mono

interface PetPersistenceGateway {
    fun save(pet: Pet): Mono<Pet>
    fun countByOwner(userId: String): Mono<Long>
    fun findById(petId: String): Mono<Pet>
}