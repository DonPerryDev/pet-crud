package com.donperry.rest.pet.dto

import java.time.LocalDate

data class PetResponse(
    val id: String,
    val name: String,
    val species: String,
    val breed: String?,
    val age: Int,
    val owner: String,
    val registrationDate: LocalDate
)