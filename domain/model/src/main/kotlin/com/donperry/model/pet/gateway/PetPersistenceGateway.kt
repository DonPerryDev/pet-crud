package com.donperry.model.pet.gateway

import com.donperry.model.pet.Pet

interface PetPersistenceGateway {
    fun save(pet: Pet): Pet
}