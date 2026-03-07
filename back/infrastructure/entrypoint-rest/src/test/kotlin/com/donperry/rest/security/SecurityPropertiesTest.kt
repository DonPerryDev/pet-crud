package com.donperry.rest.security

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SecurityPropertiesTest {
    @Test
    fun `SecurityProperties should have default empty allowed origins`() {
        val securityProperties = SecurityProperties()

        assertTrue(securityProperties.allowedOrigins.isEmpty())
    }

    @Test
    fun `SecurityProperties should allow setting allowed origins`() {
        val securityProperties = SecurityProperties()
        val allowedOrigins = listOf("https://example.com", "https://test.com")

        securityProperties.allowedOrigins = allowedOrigins

        assertEquals(allowedOrigins, securityProperties.allowedOrigins)
        assertEquals(2, securityProperties.allowedOrigins.size)
        assertTrue(securityProperties.allowedOrigins.contains("https://example.com"))
        assertTrue(securityProperties.allowedOrigins.contains("https://test.com"))
    }

    @Test
    fun `SecurityProperties should handle single allowed origin`() {
        val securityProperties = SecurityProperties()
        val allowedOrigins = listOf("https://single-origin.com")

        securityProperties.allowedOrigins = allowedOrigins

        assertEquals(allowedOrigins, securityProperties.allowedOrigins)
        assertEquals(1, securityProperties.allowedOrigins.size)
        assertEquals("https://single-origin.com", securityProperties.allowedOrigins.first())
    }

    @Test
    fun `SecurityProperties should handle empty list assignment`() {
        val securityProperties = SecurityProperties()

        securityProperties.allowedOrigins = emptyList()

        assertTrue(securityProperties.allowedOrigins.isEmpty())
    }

    @Test
    fun `SecurityProperties should handle multiple allowed origins`() {
        val securityProperties = SecurityProperties()
        val allowedOrigins =
            listOf(
                "https://example.com",
                "https://test.com",
                "https://dev.com",
                "http://localhost:3000",
                "http://localhost:8080",
            )

        securityProperties.allowedOrigins = allowedOrigins

        assertEquals(allowedOrigins, securityProperties.allowedOrigins)
        assertEquals(5, securityProperties.allowedOrigins.size)
        allowedOrigins.forEach { origin ->
            assertTrue(securityProperties.allowedOrigins.contains(origin))
        }
    }
}
