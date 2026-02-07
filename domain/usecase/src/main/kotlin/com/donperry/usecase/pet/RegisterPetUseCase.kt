package com.donperry.usecase.pet

import com.donperry.model.pet.Pet
import com.donperry.model.pet.gateway.PetPersistenceGateway
import reactor.core.publisher.Mono
import java.time.LocalDate
import java.util.UUID

class RegisterPetUseCase(
    private val petPersistenceGateway: PetPersistenceGateway
) {
    fun execute(
        name: String,
        species: String,
        breed: String?,
        age: Int,
        owner: String
    ): Mono<Pet> {
        val pet = Pet(
            id = UUID.randomUUID().toString(),
            name = name,
            species = species,
            breed = breed,
            age = age,
            owner = owner,
            registrationDate = LocalDate.now()
        )
        
       return petPersistenceGateway.save(pet)
    }
}