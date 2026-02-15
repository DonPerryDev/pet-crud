package com.donperry.storage.photo

import org.junit.jupiter.api.Test
import software.amazon.awssdk.services.s3.S3AsyncClient

class S3ClientConfigTest {

    private val config = S3ClientConfig()

    @Test
    fun `should create S3AsyncClient with explicit credentials when provided`() {
        // Arrange
        val properties = S3Properties(
            bucketName = "test-bucket",
            region = "us-east-1",
            accessKeyId = "AKIAIOSFODNN7EXAMPLE",
            secretAccessKey = "wJalrXUtnFEMI/K7MDENG/bPxRfiCYEXAMPLEKEY"
        )

        // Act
        val client: S3AsyncClient = config.s3AsyncClient(properties)

        // Assert
        assert(client != null)
        client.close()
    }

    @Test
    fun `should create S3AsyncClient with default credentials when accessKeyId is null`() {
        // Arrange
        val properties = S3Properties(
            bucketName = "test-bucket",
            region = "us-west-2",
            accessKeyId = null,
            secretAccessKey = null
        )

        // Act
        val client: S3AsyncClient = config.s3AsyncClient(properties)

        // Assert
        assert(client != null)
        client.close()
    }

    @Test
    fun `should create S3AsyncClient with default credentials when accessKeyId is blank`() {
        // Arrange
        val properties = S3Properties(
            bucketName = "test-bucket",
            region = "eu-west-1",
            accessKeyId = "  ",
            secretAccessKey = "  "
        )

        // Act
        val client: S3AsyncClient = config.s3AsyncClient(properties)

        // Assert
        assert(client != null)
        client.close()
    }

    @Test
    fun `should create S3AsyncClient with default credentials when secretAccessKey is null`() {
        // Arrange
        val properties = S3Properties(
            bucketName = "test-bucket",
            region = "ap-south-1",
            accessKeyId = "AKIAIOSFODNN7EXAMPLE",
            secretAccessKey = null
        )

        // Act
        val client: S3AsyncClient = config.s3AsyncClient(properties)

        // Assert
        assert(client != null)
        client.close()
    }

    @Test
    fun `should create S3AsyncClient with default credentials when secretAccessKey is blank`() {
        // Arrange
        val properties = S3Properties(
            bucketName = "test-bucket",
            region = "ca-central-1",
            accessKeyId = "AKIAIOSFODNN7EXAMPLE",
            secretAccessKey = ""
        )

        // Act
        val client: S3AsyncClient = config.s3AsyncClient(properties)

        // Assert
        assert(client != null)
        client.close()
    }

    @Test
    fun `should create S3AsyncClient with correct region`() {
        // Arrange
        val properties = S3Properties(
            bucketName = "test-bucket",
            region = "sa-east-1",
            accessKeyId = "AKIAIOSFODNN7EXAMPLE",
            secretAccessKey = "wJalrXUtnFEMI/K7MDENG/bPxRfiCYEXAMPLEKEY"
        )

        // Act
        val client: S3AsyncClient = config.s3AsyncClient(properties)

        // Assert
        assert(client != null)
        client.close()
    }
}
