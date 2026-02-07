package com.donperry.rest.security

import com.donperry.app.security.CorsGlobalConfig
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.junit.jupiter.MockitoExtension
import kotlin.test.assertNotNull

@ExtendWith(MockitoExtension::class)
class CorsGlobalConfigTest {
    @Mock
    private lateinit var securityProperties: SecurityProperties

    private lateinit var corsGlobalConfig: CorsGlobalConfig

    @BeforeEach
    fun setUp() {
        corsGlobalConfig = CorsGlobalConfig(securityProperties)
    }

    @Test
    fun `corsWebFilter should create CorsWebFilter with correct configuration`() {
        val allowedOrigins = listOf("https://example.com", "https://test.com")
        `when`(securityProperties.allowedOrigins).thenReturn(allowedOrigins)

        val corsWebFilter = corsGlobalConfig.corsWebFilter()

        assertNotNull(corsWebFilter)
    }

    @Test
    fun `corsWebFilter should handle empty allowed origins`() {
        `when`(securityProperties.allowedOrigins).thenReturn(emptyList())

        val corsWebFilter = corsGlobalConfig.corsWebFilter()

        assertNotNull(corsWebFilter)
    }

    @Test
    fun `corsWebFilter should handle single allowed origin`() {
        val allowedOrigins = listOf("https://single-origin.com")
        `when`(securityProperties.allowedOrigins).thenReturn(allowedOrigins)

        val corsWebFilter = corsGlobalConfig.corsWebFilter()

        assertNotNull(corsWebFilter)
    }

    @Test
    fun `corsWebFilter should handle multiple allowed origins`() {
        val allowedOrigins =
            listOf(
                "https://example.com",
                "https://test.com",
                "https://dev.com",
                "http://localhost:3000",
            )
        `when`(securityProperties.allowedOrigins).thenReturn(allowedOrigins)

        val corsWebFilter = corsGlobalConfig.corsWebFilter()

        assertNotNull(corsWebFilter)
    }
}
