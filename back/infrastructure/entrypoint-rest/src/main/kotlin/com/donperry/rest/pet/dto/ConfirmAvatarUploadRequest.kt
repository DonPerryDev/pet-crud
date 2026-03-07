package com.donperry.rest.pet.dto

import com.donperry.model.pet.ConfirmAvatarUploadCommand
import com.donperry.rest.common.validation.Validated

data class ConfirmAvatarUploadRequest(
    val contentType: String
)

fun ConfirmAvatarUploadRequest.validate(
    userId: String,
    petId: String
): Validated<ConfirmAvatarUploadCommand> {
    if (contentType.isBlank()) return Validated.Invalid("Content type cannot be blank")

    return Validated.Valid(ConfirmAvatarUploadCommand(userId, petId, contentType))
}
