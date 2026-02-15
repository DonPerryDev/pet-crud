package com.donperry.model.pet

import java.math.BigDecimal
import java.time.LocalDate

data class RegisterPetCommand(
    val userId: String,
    val name: String,
    val species: Species,
    val breed: String?,
    val age: Int,
    val birthdate: LocalDate?,
    val weight: BigDecimal?,
    val nickname: String?,
    val photo: PhotoUploadData?
)
