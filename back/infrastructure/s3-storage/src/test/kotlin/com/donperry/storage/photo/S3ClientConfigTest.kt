package com.donperry.storage.photo

import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test

class S3ClientConfigTest {

    private val config = S3ClientConfig()

    @Test
    fun `should create S3AsyncClient instance`() {
        val client = config.s3AsyncClient()

        assertNotNull(client)
        client.close()
    }

    @Test
    fun `should create S3Presigner instance`() {
        val presigner = config.s3Presigner()

        assertNotNull(presigner)
        presigner.close()
    }
}
