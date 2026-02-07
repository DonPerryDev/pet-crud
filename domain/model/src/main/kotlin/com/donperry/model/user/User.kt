package com.donperry.model.user

import java.util.UUID

data class User(
    val id: UUID,
    val email: String,
)
