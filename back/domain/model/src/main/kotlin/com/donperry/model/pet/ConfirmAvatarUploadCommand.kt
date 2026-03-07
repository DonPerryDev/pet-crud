package com.donperry.model.pet

data class ConfirmAvatarUploadCommand(
    val userId: String,
    val petId: String,
    val contentType: String
)
