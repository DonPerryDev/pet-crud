package com.donperry.rest.pet.dto

import com.donperry.model.pet.RegisterPetCommand
import com.donperry.model.pet.Species
import com.donperry.rest.common.validation.Validated
import java.math.BigDecimal
import java.time.LocalDate

data class RegisterPetRequest(
    val name: String,
    val species: String,
    val breed: String?,
    val age: Int,
    val birthdate: LocalDate?,
    val weight: BigDecimal?,
    val nickname: String?
)

fun RegisterPetRequest.validate(userId: String): Validated<RegisterPetCommand> {
    val error = when {
        name.isBlank() -> "Pet name cannot be blank"
        species.isBlank() -> "Pet species cannot be blank"
        breed?.isBlank() == true -> "Pet breed cannot be blank"
        nickname?.isBlank() == true -> "Pet nickname cannot be blank"
        else -> null
    }
    if (error != null) return Validated.Invalid(error)

    val parsedSpecies = Species.entries.find { it.name.equals(species, ignoreCase = true) }
        ?: return Validated.Invalid(
            "Invalid species: $species. Must be one of: ${Species.entries.joinToString()}"
        )

    return Validated.Valid(
        RegisterPetCommand(userId, name, parsedSpecies, breed, age, birthdate, weight, nickname)
    )
}
