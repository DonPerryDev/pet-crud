package com.donperry.rest.security

import com.donperry.model.exception.UnauthorizedException
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import java.util.Base64

/**
 * Extracts user_id from JWT payload via Base64 decoding.
 * Does NOT verify JWT signature — assumes tokens are pre-validated upstream.
 */
object JwtUtils {

    private val objectMapper = jacksonObjectMapper()

    fun extractUserId(authorizationHeader: String?): String {
        if (authorizationHeader.isNullOrBlank() || !authorizationHeader.startsWith("Bearer ")) {
            throw UnauthorizedException("Missing or invalid Authorization header")
        }

        val token = authorizationHeader.removePrefix("Bearer ").trim()
        if (token.isBlank()) {
            throw UnauthorizedException("Missing or invalid Authorization header")
        }

        val parts = token.split(".")
        if (parts.size != 3) {
            throw UnauthorizedException("Invalid JWT format")
        }

        return try {
            val payload = String(Base64.getUrlDecoder().decode(parts[1]))
            val claims: Map<String, Any> = objectMapper.readValue(payload)
            claims["user_id"]?.toString()
                ?: throw UnauthorizedException("Missing user_id claim in JWT")
        } catch (e: UnauthorizedException) {
            throw e
        } catch (e: Exception) {
            throw UnauthorizedException("Failed to decode JWT payload")
        }
    }
}
