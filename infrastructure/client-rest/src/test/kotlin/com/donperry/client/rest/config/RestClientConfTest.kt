package com.donperry.client.rest.config

import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.test.util.ReflectionTestUtils
import org.springframework.web.reactive.function.client.WebClient

class RestClientConfTest {
    private lateinit var restClientConf: RestClientConf

    @BeforeEach
    fun setUp() {
        restClientConf = RestClientConf()
    }

    @Test
    fun `should create WebClient bean with base URL`() {
        val userHost = "https://api.example.com"
        ReflectionTestUtils.setField(restClientConf, "userHost", userHost)

        val result = restClientConf.WebClient()

        assertNotNull(result)
    }

    @Test
    fun `should create WebClient bean with different base URL`() {
        val userHost = "https://localhost:8080"
        ReflectionTestUtils.setField(restClientConf, "userHost", userHost)

        val result = restClientConf.WebClient()

        assertNotNull(result)
    }

    @Test
    fun `should create WebClient bean with HTTP URL`() {
        val userHost = "http://internal-service:3000"
        ReflectionTestUtils.setField(restClientConf, "userHost", userHost)

        val result = restClientConf.WebClient()

        assertNotNull(result)
    }

    @Test
    fun `should create WebClient bean with empty URL`() {
        val userHost = ""
        ReflectionTestUtils.setField(restClientConf, "userHost", userHost)

        val result = restClientConf.WebClient()

        assertNotNull(result)
    }
}
