package com.donperry.rest.pet.dto

import com.donperry.model.pet.GeneratePresignedUrlCommand
import com.donperry.rest.common.validation.Validated

data class GeneratePresignedUrlRequest(
    val contentType: String
)

fun GeneratePresignedUrlRequest.validate(
    userId: String,
    petId: String
): Validated<GeneratePresignedUrlCommand> {
    if (contentType.isBlank()) return Validated.Invalid("Content type cannot be blank")

    return Validated.Valid(GeneratePresignedUrlCommand(userId, petId, contentType))
}
