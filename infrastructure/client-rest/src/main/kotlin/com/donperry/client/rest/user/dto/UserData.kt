package com.donperry.client.rest.user.dto

import java.util.UUID

data class UserData(
    val id: UUID,
    val email: String,
)
