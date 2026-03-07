package com.donperry.rest.common.dto

data class ErrorResponse(
    val error: String,
    val message: String,
    val timestamp: String
)
