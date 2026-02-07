package com.donperry.persistence.pet.mapper

import com.donperry.model.pet.Pet
import com.donperry.persistence.pet.entities.PetData
import java.util.*

class PetMapper {
    companion object {
        fun toEntity(petModel: Pet): PetData =
            PetData(
                id = petModel.id?.let { UUID.fromString(it) },
                name = petModel.name,
                species = petModel.species,
                breed = petModel.breed,
                age = petModel.age,
                owner = petModel.owner,
                registrationDate = petModel.registrationDate,
            )

        fun toModel(petData: PetData): Pet =
            Pet(
                id = petData.id.toString(),
                name = petData.name,
                species = petData.species,
                breed = petData.breed,
                age = petData.age,
                owner = petData.owner,
                registrationDate = petData.registrationDate,
            )

    }
}