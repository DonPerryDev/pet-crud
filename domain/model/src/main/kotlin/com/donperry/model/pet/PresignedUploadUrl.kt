package com.donperry.model.pet

import java.time.Instant

data class PresignedUploadUrl(
    val uploadUrl: String,
    val key: String,
    val expiresAt: Instant
)
