package com.donperry.persistence.pet

import com.donperry.model.pet.Pet
import com.donperry.model.pet.gateway.PetPersistenceGateway
import org.springframework.stereotype.Repository
import java.util.concurrent.ConcurrentHashMap

@Repository
class InMemoryPetPersistenceGateway : PetPersistenceGateway {
    private val pets = ConcurrentHashMap<String, Pet>()

    override fun save(pet: Pet): Pet {
        pets[pet.id] = pet
        return pet
    }

}