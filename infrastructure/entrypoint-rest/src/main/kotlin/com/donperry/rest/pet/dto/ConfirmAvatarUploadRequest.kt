package com.donperry.rest.pet.dto

import com.donperry.model.pet.ConfirmAvatarUploadCommand
import com.donperry.rest.common.validation.Validated

data class ConfirmAvatarUploadRequest(
    val photoKey: String
)

fun ConfirmAvatarUploadRequest.validate(
    userId: String,
    petId: String
): Validated<ConfirmAvatarUploadCommand> {
    if (photoKey.isBlank()) return Validated.Invalid("Photo key cannot be blank")

    return Validated.Valid(ConfirmAvatarUploadCommand(userId, petId, photoKey))
}
