package com.donperry.model.pet

import java.time.LocalDate

data class Pet(
    val id: String?=null,
    val name: String,
    val species: String,
    val breed: String?,
    val age: Int,
    val owner: String,
    val registrationDate: LocalDate,
)