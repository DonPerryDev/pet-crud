package com.donperry.rest.pet.dto

data class RegisterPetRequest(
    val name: String,
    val species: String,
    val breed: String?,
    val age: Int,
    val owner: String
)