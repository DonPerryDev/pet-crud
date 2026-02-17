package com.donperry.model.pet

import java.math.BigDecimal
import java.time.LocalDate

data class UpdatePetCommand(
    val petId: String,
    val userId: String,
    val name: String,
    val species: Species,
    val breed: String?,
    val age: Int,
    val birthdate: LocalDate?,
    val weight: BigDecimal?,
    val nickname: String?,
)
