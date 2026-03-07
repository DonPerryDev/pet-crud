package com.donperry.rest.pet.dto

import java.math.BigDecimal
import java.time.LocalDate

data class PetDetailResponse(
    val id: String,
    val name: String,
    val species: String,
    val breed: String?,
    val age: Int,
    val birthdate: LocalDate?,
    val weight: BigDecimal?,
    val nickname: String?,
    val owner: String,
    val registrationDate: LocalDate,
    val photoUrl: String?
)
