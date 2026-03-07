package com.donperry.model.pet

data class DeletePetCommand(
    val petId: String,
    val userId: String
)
