package com.donperry.storage.photo

import org.junit.jupiter.api.Test
import software.amazon.awssdk.services.s3.S3AsyncClient
import software.amazon.awssdk.services.s3.presigner.S3Presigner

class S3ClientConfigTest {

    private val config = S3ClientConfig()

    @Test
    fun `should create non-null S3AsyncClient instance`() {
        val client: S3AsyncClient = config.s3AsyncClient()

        assert(client != null)
        client.close()
    }

    @Test
    fun `should create non-null S3Presigner instance`() {
        val presigner: S3Presigner = config.s3Presigner()

        assert(presigner != null)
        presigner.close()
    }
}
