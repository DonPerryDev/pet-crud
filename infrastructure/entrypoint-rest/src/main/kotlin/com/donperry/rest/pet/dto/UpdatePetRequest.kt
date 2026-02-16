package com.donperry.rest.pet.dto

import com.donperry.model.pet.Species
import com.donperry.model.pet.UpdatePetCommand
import com.donperry.rest.common.validation.Validated
import java.math.BigDecimal
import java.time.LocalDate

data class UpdatePetRequest(
    val name: String,
    val species: String,
    val breed: String?,
    val age: Int,
    val birthdate: LocalDate?,
    val weight: BigDecimal?,
    val nickname: String?,
    val photoUrl: String?
)

fun UpdatePetRequest.validate(petId: String, userId: String): Validated<UpdatePetCommand> {
    val error = when {
        petId.isBlank() -> "Pet ID cannot be blank"
        name.isBlank() -> "Pet name cannot be blank"
        species.isBlank() -> "Pet species cannot be blank"
        breed?.isBlank() == true -> "Pet breed cannot be blank"
        nickname?.isBlank() == true -> "Pet nickname cannot be blank"
        age < 0 -> "Pet age must be zero or greater"
        weight != null && weight <= BigDecimal.ZERO -> "Pet weight must be greater than zero"
        else -> null
    }
    if (error != null) return Validated.Invalid(error)

    val parsedSpecies = Species.entries.find { it.name.equals(species, ignoreCase = true) }
        ?: return Validated.Invalid(
            "Invalid species: $species. Must be one of: ${Species.entries.joinToString()}"
        )

    return Validated.Valid(
        UpdatePetCommand(petId, userId, name, parsedSpecies, breed, age, birthdate, weight, nickname, photoUrl)
    )
}
