package com.donperry.rest.pet.dto

data class PresignedUrlResponse(
    val uploadUrl: String,
    val key: String,
    val expiresAt: String
)
