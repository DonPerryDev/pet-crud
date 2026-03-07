package com.donperry.model.pet

data class GeneratePresignedUrlCommand(
    val userId: String,
    val petId: String,
    val contentType: String
)
