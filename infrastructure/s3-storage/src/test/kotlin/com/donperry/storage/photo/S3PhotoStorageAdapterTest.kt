package com.donperry.storage.photo

import com.donperry.model.exception.PhotoUploadException
import com.donperry.model.pet.PhotoUploadData
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.verify
import reactor.test.StepVerifier
import software.amazon.awssdk.core.async.AsyncRequestBody
import software.amazon.awssdk.services.s3.S3AsyncClient
import software.amazon.awssdk.services.s3.model.PutObjectRequest
import software.amazon.awssdk.services.s3.model.PutObjectResponse
import java.util.concurrent.CompletableFuture

@ExtendWith(MockitoExtension::class)
class S3PhotoStorageAdapterTest {

    @Mock
    private lateinit var s3AsyncClient: S3AsyncClient

    @Mock
    private lateinit var s3Properties: S3Properties

    @InjectMocks
    private lateinit var adapter: S3PhotoStorageAdapter

    @Test
    fun `should upload photo and return S3 URL when successful`() {
        // Arrange
        val userId = "user123"
        val petId = "pet456"
        val photoData = PhotoUploadData(
            fileName = "dog.jpg",
            contentType = "image/jpeg",
            fileSize = 1024L,
            fileBytes = ByteArray(1024)
        )

        val bucketName = "my-pet-bucket"
        val region = "us-east-1"
        val expectedUrl = "https://$bucketName.s3.$region.amazonaws.com/pets/$userId/$petId/dog.jpg"

        `when`(s3Properties.bucketName).thenReturn(bucketName)
        `when`(s3Properties.region).thenReturn(region)

        val mockResponse = PutObjectResponse.builder().build()
        val completedFuture = CompletableFuture.completedFuture(mockResponse)
        `when`(s3AsyncClient.putObject(any<PutObjectRequest>(), any<AsyncRequestBody>())).thenReturn(completedFuture)

        // Act & Assert
        StepVerifier.create(adapter.uploadPhoto(userId, petId, photoData))
            .expectNext(expectedUrl)
            .verifyComplete()
    }

    @Test
    fun `should set correct PutObjectRequest parameters`() {
        // Arrange
        val userId = "user123"
        val petId = "pet456"
        val photoData = PhotoUploadData(
            fileName = "cat.png",
            contentType = "image/png",
            fileSize = 2048L,
            fileBytes = ByteArray(2048)
        )

        val bucketName = "my-pet-bucket"
        val region = "eu-west-1"

        `when`(s3Properties.bucketName).thenReturn(bucketName)
        `when`(s3Properties.region).thenReturn(region)

        val mockResponse = PutObjectResponse.builder().build()
        val completedFuture = CompletableFuture.completedFuture(mockResponse)
        `when`(s3AsyncClient.putObject(any<PutObjectRequest>(), any<AsyncRequestBody>())).thenReturn(completedFuture)

        // Act
        adapter.uploadPhoto(userId, petId, photoData).block()

        // Assert
        val requestCaptor = argumentCaptor<PutObjectRequest>()
        verify(s3AsyncClient).putObject(requestCaptor.capture(), any<AsyncRequestBody>())

        val capturedRequest = requestCaptor.firstValue
        assert(capturedRequest.bucket() == bucketName)
        assert(capturedRequest.key() == "pets/$userId/$petId/cat.png")
        assert(capturedRequest.contentType() == "image/png")
        assert(capturedRequest.contentLength() == 2048L)
    }

    @Test
    fun `should use correct key format with userId, petId, and fileName`() {
        // Arrange
        val userId = "user-abc"
        val petId = "pet-xyz"
        val photoData = PhotoUploadData(
            fileName = "photo.jpg",
            contentType = "image/jpeg",
            fileSize = 512L,
            fileBytes = ByteArray(512)
        )

        val bucketName = "test-bucket"
        val region = "ap-south-1"

        `when`(s3Properties.bucketName).thenReturn(bucketName)
        `when`(s3Properties.region).thenReturn(region)

        val mockResponse = PutObjectResponse.builder().build()
        val completedFuture = CompletableFuture.completedFuture(mockResponse)
        `when`(s3AsyncClient.putObject(any<PutObjectRequest>(), any<AsyncRequestBody>())).thenReturn(completedFuture)

        // Act
        adapter.uploadPhoto(userId, petId, photoData).block()

        // Assert
        val requestCaptor = argumentCaptor<PutObjectRequest>()
        verify(s3AsyncClient).putObject(requestCaptor.capture(), any<AsyncRequestBody>())

        val expectedKey = "pets/$userId/$petId/photo.jpg"
        assert(requestCaptor.firstValue.key() == expectedKey)
    }

    @Test
    fun `should map S3 error to PhotoUploadException`() {
        // Arrange
        val userId = "user123"
        val petId = "pet456"
        val photoData = PhotoUploadData(
            fileName = "dog.jpg",
            contentType = "image/jpeg",
            fileSize = 1024L,
            fileBytes = ByteArray(1024)
        )

        // Note: bucketName and region are not needed for this test as the S3 call fails before URL construction
        val s3Exception = RuntimeException("S3 connection failed")
        val failedFuture = CompletableFuture<PutObjectResponse>()
        failedFuture.completeExceptionally(s3Exception)
        `when`(s3AsyncClient.putObject(any<PutObjectRequest>(), any<AsyncRequestBody>())).thenReturn(failedFuture)

        // Act & Assert
        StepVerifier.create(adapter.uploadPhoto(userId, petId, photoData))
            .expectError(PhotoUploadException::class.java)
            .verify()
    }

    @Test
    fun `should wrap S3 error with correct message`() {
        // Arrange
        val userId = "user123"
        val petId = "pet456"
        val photoData = PhotoUploadData(
            fileName = "test.jpg",
            contentType = "image/jpeg",
            fileSize = 100L,
            fileBytes = ByteArray(100)
        )

        // Note: bucketName and region are not needed for this test as the S3 call fails before URL construction
        val s3Exception = RuntimeException("Access denied")
        val failedFuture = CompletableFuture<PutObjectResponse>()
        failedFuture.completeExceptionally(s3Exception)
        `when`(s3AsyncClient.putObject(any<PutObjectRequest>(), any<AsyncRequestBody>())).thenReturn(failedFuture)

        // Act & Assert
        StepVerifier.create(adapter.uploadPhoto(userId, petId, photoData))
            .expectErrorMatches { error ->
                error is PhotoUploadException &&
                error.message == "Failed to upload photo to S3" &&
                error.cause == s3Exception
            }
            .verify()
    }

    @Test
    fun `should construct URL with correct bucket and region`() {
        // Arrange
        val userId = "test-user"
        val petId = "test-pet"
        val photoData = PhotoUploadData(
            fileName = "image.jpg",
            contentType = "image/jpeg",
            fileSize = 256L,
            fileBytes = ByteArray(256)
        )

        val bucketName = "prod-bucket"
        val region = "ca-central-1"

        `when`(s3Properties.bucketName).thenReturn(bucketName)
        `when`(s3Properties.region).thenReturn(region)

        val mockResponse = PutObjectResponse.builder().build()
        val completedFuture = CompletableFuture.completedFuture(mockResponse)
        `when`(s3AsyncClient.putObject(any<PutObjectRequest>(), any<AsyncRequestBody>())).thenReturn(completedFuture)

        // Act & Assert
        StepVerifier.create(adapter.uploadPhoto(userId, petId, photoData))
            .expectNext("https://$bucketName.s3.$region.amazonaws.com/pets/$userId/$petId/image.jpg")
            .verifyComplete()
    }

    @Test
    fun `should handle photo with special characters in fileName`() {
        // Arrange
        val userId = "user123"
        val petId = "pet456"
        val photoData = PhotoUploadData(
            fileName = "my pet photo 2024.jpg",
            contentType = "image/jpeg",
            fileSize = 512L,
            fileBytes = ByteArray(512)
        )

        `when`(s3Properties.bucketName).thenReturn("bucket")
        `when`(s3Properties.region).thenReturn("us-east-1")

        val mockResponse = PutObjectResponse.builder().build()
        val completedFuture = CompletableFuture.completedFuture(mockResponse)
        `when`(s3AsyncClient.putObject(any<PutObjectRequest>(), any<AsyncRequestBody>())).thenReturn(completedFuture)

        // Act
        adapter.uploadPhoto(userId, petId, photoData).block()

        // Assert
        val requestCaptor = argumentCaptor<PutObjectRequest>()
        verify(s3AsyncClient).putObject(requestCaptor.capture(), any<AsyncRequestBody>())

        assert(requestCaptor.firstValue.key() == "pets/$userId/$petId/my pet photo 2024.jpg")
    }
}
