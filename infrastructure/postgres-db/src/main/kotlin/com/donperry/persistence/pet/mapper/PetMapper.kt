package com.donperry.persistence.pet.mapper

import com.donperry.model.pet.Pet
import com.donperry.model.pet.Species
import com.donperry.persistence.pet.entities.PetData
import java.util.*

object PetMapper {
    fun toEntity(petModel: Pet): PetData =
        PetData(
            id = petModel.id?.let { UUID.fromString(it) },
            name = petModel.name,
            species = petModel.species.name,
            breed = petModel.breed,
            age = petModel.age,
            birthdate = petModel.birthdate,
            weight = petModel.weight,
            nickname = petModel.nickname,
            owner = petModel.owner,
            registrationDate = petModel.registrationDate,
            photoUrl = petModel.photoUrl
        )

    fun toModel(petData: PetData): Pet =
        Pet(
            id = petData.id.toString(),
            name = petData.name,
            species = Species.valueOf(petData.species),
            breed = petData.breed,
            age = petData.age,
            birthdate = petData.birthdate,
            weight = petData.weight,
            nickname = petData.nickname,
            owner = petData.owner,
            registrationDate = petData.registrationDate,
            photoUrl = petData.photoUrl
        )
}
