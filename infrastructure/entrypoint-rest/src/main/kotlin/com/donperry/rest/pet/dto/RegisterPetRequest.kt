package com.donperry.rest.pet.dto

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
