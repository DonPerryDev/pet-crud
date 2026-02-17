package com.donperry.rest.pet.dto

data class PetListResponse(
    val id: String,
    val name: String,
    val species: String,
    val breed: String?,
    val photoUrl: String?
)
