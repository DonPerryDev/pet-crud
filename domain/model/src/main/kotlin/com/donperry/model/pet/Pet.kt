package com.donperry.model.pet

import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

data class Pet(
    val id: String? = null,
    val name: String,
    val species: Species,
    val breed: String?,
    val age: Int,
    val birthdate: LocalDate? = null,
    val weight: BigDecimal? = null,
    val nickname: String? = null,
    val owner: String,
    val registrationDate: LocalDate,
    val photoUrl: String? = null,
    val deletedAt: LocalDateTime? = null
)
