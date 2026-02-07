package com.donperry.usecase.pet

import com.donperry.model.pet.Pet
import com.donperry.model.pet.gateway.PetPersistenceGateway
import reactor.core.publisher.Mono
import java.time.LocalDate
import java.util.logging.Logger

class RegisterPetUseCase(
    private val petPersistenceGateway: PetPersistenceGateway
) {
    companion object {
        private val logger: Logger = Logger.getLogger(RegisterPetUseCase::class.java.name)
    }

    fun execute(
        name: String,
        species: String,
        breed: String?,
        age: Int,
        owner: String
    ): Mono<Pet> {
        logger.info("Starting pet registration process for pet: $name")
        logger.fine("Pet details - species: $species, breed: $breed, age: $age, owner: $owner")
        
        val pet = Pet(
            name = name,
            species = species,
            breed = breed,
            age = age,
            owner = owner,
            registrationDate = LocalDate.now()
        )
        
        logger.info("Created pet entity, proceeding to save in database")
        
       return petPersistenceGateway.save(pet)
           .doOnNext { savedPet ->
               logger.info("Pet successfully saved to database with ID: ${savedPet.id}")
           }
           .doOnError { error ->
               logger.warning("Failed to save pet to database: ${error.message}")
               logger.fine("Error details: ${error.stackTraceToString()}")
           }
    }
}