package com.donperry.rest.security

import com.donperry.model.exception.UnauthorizedException
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.util.Base64
import kotlin.test.assertEquals

class JwtUtilsTest {

    private val objectMapper = jacksonObjectMapper()

    private fun buildJwt(payload: Map<String, Any>): String {
        val header = Base64.getUrlEncoder().withoutPadding().encodeToString("""{"alg":"none"}""".toByteArray())
        val payloadEncoded = Base64.getUrlEncoder().withoutPadding().encodeToString(
            objectMapper.writeValueAsBytes(payload)
        )
        return "$header.$payloadEncoded.signature"
    }

    @Test
    fun `should throw UnauthorizedException when authorization header is null`() {
        val exception = assertThrows<UnauthorizedException> {
            JwtUtils.extractUserId(null)
        }
        assertEquals("Missing or invalid Authorization header", exception.message)
    }

    @Test
    fun `should throw UnauthorizedException when authorization header is blank`() {
        val exception = assertThrows<UnauthorizedException> {
            JwtUtils.extractUserId("")
        }
        assertEquals("Missing or invalid Authorization header", exception.message)
    }

    @Test
    fun `should throw UnauthorizedException when authorization header does not start with Bearer prefix`() {
        val jwt = buildJwt(mapOf("user_id" to "user-123"))

        val exception = assertThrows<UnauthorizedException> {
            JwtUtils.extractUserId(jwt)
        }
        assertEquals("Missing or invalid Authorization header", exception.message)
    }

    @Test
    fun `should throw UnauthorizedException when token after Bearer prefix is blank`() {
        val exception = assertThrows<UnauthorizedException> {
            JwtUtils.extractUserId("Bearer ")
        }
        assertEquals("Missing or invalid Authorization header", exception.message)
    }

    @Test
    fun `should throw UnauthorizedException when token after Bearer prefix is only whitespace`() {
        val exception = assertThrows<UnauthorizedException> {
            JwtUtils.extractUserId("Bearer   ")
        }
        assertEquals("Missing or invalid Authorization header", exception.message)
    }

    @Test
    fun `should throw UnauthorizedException when token has fewer than 3 parts`() {
        val exception = assertThrows<UnauthorizedException> {
            JwtUtils.extractUserId("Bearer header.payload")
        }
        assertEquals("Invalid JWT format", exception.message)
    }

    @Test
    fun `should throw UnauthorizedException when token has more than 3 parts`() {
        val exception = assertThrows<UnauthorizedException> {
            JwtUtils.extractUserId("Bearer header.payload.signature.extra")
        }
        assertEquals("Invalid JWT format", exception.message)
    }

    @Test
    fun `should return user_id when valid JWT with user_id claim is provided`() {
        val expectedUserId = "user-123"
        val jwt = buildJwt(mapOf("user_id" to expectedUserId))
        val authHeader = "Bearer $jwt"

        val actualUserId = JwtUtils.extractUserId(authHeader)

        assertEquals(expectedUserId, actualUserId)
    }

    @Test
    fun `should throw UnauthorizedException when valid JWT does not contain user_id claim`() {
        val jwt = buildJwt(mapOf("sub" to "user-123", "email" to "test@example.com"))
        val authHeader = "Bearer $jwt"

        val exception = assertThrows<UnauthorizedException> {
            JwtUtils.extractUserId(authHeader)
        }
        assertEquals("Missing user_id claim in JWT", exception.message)
    }

    @Test
    fun `should throw UnauthorizedException when JWT payload is invalid base64`() {
        val authHeader = "Bearer header.invalid!!!base64.signature"

        val exception = assertThrows<UnauthorizedException> {
            JwtUtils.extractUserId(authHeader)
        }
        assertEquals("Failed to decode JWT payload", exception.message)
    }

    @Test
    fun `should throw UnauthorizedException when JWT payload is not valid JSON`() {
        val invalidJsonPayload = Base64.getUrlEncoder().withoutPadding()
            .encodeToString("not-a-json".toByteArray())
        val authHeader = "Bearer header.$invalidJsonPayload.signature"

        val exception = assertThrows<UnauthorizedException> {
            JwtUtils.extractUserId(authHeader)
        }
        assertEquals("Failed to decode JWT payload", exception.message)
    }

    @Test
    fun `should handle user_id as integer in JWT payload`() {
        val jwt = buildJwt(mapOf("user_id" to 12345))
        val authHeader = "Bearer $jwt"

        val actualUserId = JwtUtils.extractUserId(authHeader)

        assertEquals("12345", actualUserId)
    }

    @Test
    fun `should handle extra whitespace around Bearer token`() {
        val expectedUserId = "user-456"
        val jwt = buildJwt(mapOf("user_id" to expectedUserId))
        val authHeader = "Bearer   $jwt   "

        val actualUserId = JwtUtils.extractUserId(authHeader)

        assertEquals(expectedUserId, actualUserId)
    }
}
