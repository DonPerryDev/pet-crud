package com.donperry.rest.common.validation

sealed class Validated<out T> {
    data class Valid<T>(val value: T) : Validated<T>()
    data class Invalid(val error: String) : Validated<Nothing>()
}
