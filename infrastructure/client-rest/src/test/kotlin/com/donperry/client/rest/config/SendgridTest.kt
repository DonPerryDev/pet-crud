package com.donperry.client.rest.config

import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.test.util.ReflectionTestUtils

class SendgridTest {
    private lateinit var sendgridConfig: Sendgrid

    @BeforeEach
    fun setUp() {
        sendgridConfig = Sendgrid()
    }

    @Test
    fun `should create SendGrid bean with API key`() {
        val apiKey = "test-api-key-123"
        ReflectionTestUtils.setField(sendgridConfig, "sendGridAPIKey", apiKey)

        val result = sendgridConfig.sendGrid()

        assertNotNull(result)
    }

    @Test
    fun `should create SendGrid bean with different API key`() {
        val apiKey = "different-api-key-456"
        ReflectionTestUtils.setField(sendgridConfig, "sendGridAPIKey", apiKey)

        val result = sendgridConfig.sendGrid()

        assertNotNull(result)
    }

    @Test
    fun `should create SendGrid bean with empty API key`() {
        val apiKey = ""
        ReflectionTestUtils.setField(sendgridConfig, "sendGridAPIKey", apiKey)

        val result = sendgridConfig.sendGrid()

        assertNotNull(result)
    }
}
