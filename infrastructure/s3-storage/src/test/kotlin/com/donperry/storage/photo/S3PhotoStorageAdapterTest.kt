package com.donperry.storage.photo

import com.donperry.model.pet.PresignedUploadUrl
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.junit.jupiter.MockitoSettings
import org.mockito.quality.Strictness
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import reactor.test.StepVerifier
import software.amazon.awssdk.services.s3.S3AsyncClient
import software.amazon.awssdk.services.s3.model.HeadObjectRequest
import software.amazon.awssdk.services.s3.model.HeadObjectResponse
import software.amazon.awssdk.services.s3.model.NoSuchKeyException
import software.amazon.awssdk.services.s3.model.S3Exception
import software.amazon.awssdk.services.s3.presigner.S3Presigner
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest
import java.net.URL
import java.time.Instant
import java.util.concurrent.CompletableFuture

@ExtendWith(MockitoExtension::class)
@MockitoSettings(strictness = Strictness.LENIENT)
class S3PhotoStorageAdapterTest {

    @Mock
    private lateinit var s3AsyncClient: S3AsyncClient

    @Mock
    private lateinit var s3Presigner: S3Presigner

    @Mock
    private lateinit var s3Properties: S3Properties

    private lateinit var adapter: S3PhotoStorageAdapter

    @BeforeEach
    fun setUp() {
        adapter = S3PhotoStorageAdapter(s3AsyncClient, s3Presigner, s3Properties)
    }

    // generatePresignedUrl tests

    @Test
    fun `should generate presigned URL with correct uploadUrl and key pattern`() {
        // Arrange
        val userId = "user123"
        val petId = "pet456"
        val contentType = "image/jpeg"
        val expirationMinutes = 15
        val bucketName = "test-bucket"
        val region = "us-east-1"

        whenever(s3Properties.bucketName).thenReturn(bucketName)
        whenever(s3Properties.region).thenReturn(region)

        val mockPresignedRequest = mock<PresignedPutObjectRequest>()
        val signedUrl = URL("https://test-bucket.s3.amazonaws.com/signed-url")
        whenever(mockPresignedRequest.url()).thenReturn(signedUrl)
        whenever(s3Presigner.presignPutObject(any<PutObjectPresignRequest>())).thenReturn(mockPresignedRequest)

        // Act & Assert
        StepVerifier.create(adapter.generatePresignedUrl(userId, petId, contentType, expirationMinutes))
            .assertNext { result ->
                assert(result.uploadUrl == signedUrl.toString())
                assert(result.key.startsWith("pets/$userId/$petId/"))
                assert(result.key.endsWith(".jpg"))
                assert(result.expiresAt.isAfter(Instant.now()))
            }
            .verifyComplete()
    }

    @Test
    fun `should generate key with jpg extension when content type is image-jpeg`() {
        // Arrange
        val userId = "user123"
        val petId = "pet456"
        val contentType = "image/jpeg"
        val expirationMinutes = 10

        whenever(s3Properties.bucketName).thenReturn("bucket")
        whenever(s3Properties.region).thenReturn("us-east-1")

        val mockPresignedRequest = mock<PresignedPutObjectRequest>()
        whenever(mockPresignedRequest.url()).thenReturn(URL("https://bucket.s3.amazonaws.com/signed"))
        whenever(s3Presigner.presignPutObject(any<PutObjectPresignRequest>())).thenReturn(mockPresignedRequest)

        // Act & Assert
        StepVerifier.create(adapter.generatePresignedUrl(userId, petId, contentType, expirationMinutes))
            .assertNext { result ->
                assert(result.key.endsWith(".jpg"))
                assert(result.key.matches(Regex("pets/$userId/$petId/[a-f0-9-]+\\.jpg")))
            }
            .verifyComplete()
    }

    @Test
    fun `should generate key with png extension when content type is image-png`() {
        // Arrange
        val userId = "user789"
        val petId = "pet012"
        val contentType = "image/png"
        val expirationMinutes = 20

        whenever(s3Properties.bucketName).thenReturn("bucket")
        whenever(s3Properties.region).thenReturn("eu-west-1")

        val mockPresignedRequest = mock<PresignedPutObjectRequest>()
        whenever(mockPresignedRequest.url()).thenReturn(URL("https://bucket.s3.amazonaws.com/signed"))
        whenever(s3Presigner.presignPutObject(any<PutObjectPresignRequest>())).thenReturn(mockPresignedRequest)

        // Act & Assert
        StepVerifier.create(adapter.generatePresignedUrl(userId, petId, contentType, expirationMinutes))
            .assertNext { result ->
                assert(result.key.endsWith(".png"))
                assert(result.key.matches(Regex("pets/$userId/$petId/[a-f0-9-]+\\.png")))
            }
            .verifyComplete()
    }

    @Test
    fun `should generate key with bin extension when content type is unknown`() {
        // Arrange
        val userId = "user555"
        val petId = "pet666"
        val contentType = "application/octet-stream"
        val expirationMinutes = 30

        whenever(s3Properties.bucketName).thenReturn("bucket")
        whenever(s3Properties.region).thenReturn("ap-south-1")

        val mockPresignedRequest = mock<PresignedPutObjectRequest>()
        whenever(mockPresignedRequest.url()).thenReturn(URL("https://bucket.s3.amazonaws.com/signed"))
        whenever(s3Presigner.presignPutObject(any<PutObjectPresignRequest>())).thenReturn(mockPresignedRequest)

        // Act & Assert
        StepVerifier.create(adapter.generatePresignedUrl(userId, petId, contentType, expirationMinutes))
            .assertNext { result ->
                assert(result.key.endsWith(".bin"))
                assert(result.key.matches(Regex("pets/$userId/$petId/[a-f0-9-]+\\.bin")))
            }
            .verifyComplete()
    }

    @Test
    fun `should generate key following pattern pets-userId-petId-uuid-extension`() {
        // Arrange
        val userId = "test-user"
        val petId = "test-pet"
        val contentType = "image/jpeg"
        val expirationMinutes = 15

        whenever(s3Properties.bucketName).thenReturn("bucket")
        whenever(s3Properties.region).thenReturn("us-west-2")

        val mockPresignedRequest = mock<PresignedPutObjectRequest>()
        whenever(mockPresignedRequest.url()).thenReturn(URL("https://bucket.s3.amazonaws.com/signed"))
        whenever(s3Presigner.presignPutObject(any<PutObjectPresignRequest>())).thenReturn(mockPresignedRequest)

        // Act & Assert
        StepVerifier.create(adapter.generatePresignedUrl(userId, petId, contentType, expirationMinutes))
            .assertNext { result ->
                val keyPattern = Regex("pets/$userId/$petId/[a-f0-9]{8}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{12}\\.jpg")
                assert(result.key.matches(keyPattern)) { "Key ${result.key} does not match expected pattern" }
            }
            .verifyComplete()
    }

    @Test
    fun `should set expiresAt in the future based on expiration minutes`() {
        // Arrange
        val userId = "user123"
        val petId = "pet456"
        val contentType = "image/png"
        val expirationMinutes = 25

        whenever(s3Properties.bucketName).thenReturn("bucket")
        whenever(s3Properties.region).thenReturn("us-east-1")

        val mockPresignedRequest = mock<PresignedPutObjectRequest>()
        whenever(mockPresignedRequest.url()).thenReturn(URL("https://bucket.s3.amazonaws.com/signed"))
        whenever(s3Presigner.presignPutObject(any<PutObjectPresignRequest>())).thenReturn(mockPresignedRequest)

        val startTime = Instant.now()

        // Act & Assert
        StepVerifier.create(adapter.generatePresignedUrl(userId, petId, contentType, expirationMinutes))
            .assertNext { result ->
                val minExpectedExpiry = startTime.plusSeconds((expirationMinutes * 60).toLong())
                val maxExpectedExpiry = minExpectedExpiry.plusSeconds(5) // Allow 5 seconds for test execution
                assert(result.expiresAt.isAfter(minExpectedExpiry.minusSeconds(1)))
                assert(result.expiresAt.isBefore(maxExpectedExpiry))
            }
            .verifyComplete()
    }

    @Test
    fun `should propagate error when S3Presigner fails`() {
        // Arrange
        val userId = "user123"
        val petId = "pet456"
        val contentType = "image/jpeg"
        val expirationMinutes = 15

        whenever(s3Properties.bucketName).thenReturn("bucket")
        whenever(s3Properties.region).thenReturn("us-east-1")

        val s3Exception = S3Exception.builder().message("Presigner error").build()
        whenever(s3Presigner.presignPutObject(any<PutObjectPresignRequest>())).thenThrow(s3Exception)

        // Act & Assert
        StepVerifier.create(adapter.generatePresignedUrl(userId, petId, contentType, expirationMinutes))
            .expectError(S3Exception::class.java)
            .verify()
    }

    // verifyPhotoExists tests

    @Test
    fun `should return true when photo exists in S3`() {
        // Arrange
        val photoKey = "pets/user123/pet456/photo.jpg"

        whenever(s3Properties.bucketName).thenReturn("test-bucket")

        val mockResponse = HeadObjectResponse.builder().build()
        val completedFuture = CompletableFuture.completedFuture(mockResponse)
        whenever(s3AsyncClient.headObject(any<HeadObjectRequest>())).thenReturn(completedFuture)

        // Act & Assert
        StepVerifier.create(adapter.verifyPhotoExists(photoKey))
            .expectNext(true)
            .verifyComplete()
    }

    @Test
    fun `should return false when photo does not exist in S3`() {
        // Arrange
        val photoKey = "pets/user123/pet456/nonexistent.jpg"

        whenever(s3Properties.bucketName).thenReturn("test-bucket")

        val noSuchKeyException = NoSuchKeyException.builder().message("Key not found").build()
        val failedFuture = CompletableFuture<HeadObjectResponse>()
        failedFuture.completeExceptionally(noSuchKeyException)
        whenever(s3AsyncClient.headObject(any<HeadObjectRequest>())).thenReturn(failedFuture)

        // Act & Assert
        StepVerifier.create(adapter.verifyPhotoExists(photoKey))
            .expectNext(false)
            .verifyComplete()
    }

    @Test
    fun `should propagate error when S3 headObject fails with non-NoSuchKey exception`() {
        // Arrange
        val photoKey = "pets/user123/pet456/photo.jpg"

        whenever(s3Properties.bucketName).thenReturn("test-bucket")

        val s3Exception = S3Exception.builder().message("Access denied").build()
        val failedFuture = CompletableFuture<HeadObjectResponse>()
        failedFuture.completeExceptionally(s3Exception)
        whenever(s3AsyncClient.headObject(any<HeadObjectRequest>())).thenReturn(failedFuture)

        // Act & Assert
        StepVerifier.create(adapter.verifyPhotoExists(photoKey))
            .expectError(S3Exception::class.java)
            .verify()
    }

    @Test
    fun `should propagate runtime error when S3 headObject fails with unexpected exception`() {
        // Arrange
        val photoKey = "pets/user123/pet456/photo.jpg"

        whenever(s3Properties.bucketName).thenReturn("test-bucket")

        val runtimeException = RuntimeException("Network error")
        val failedFuture = CompletableFuture<HeadObjectResponse>()
        failedFuture.completeExceptionally(runtimeException)
        whenever(s3AsyncClient.headObject(any<HeadObjectRequest>())).thenReturn(failedFuture)

        // Act & Assert
        StepVerifier.create(adapter.verifyPhotoExists(photoKey))
            .expectError(RuntimeException::class.java)
            .verify()
    }

    // buildPhotoUrl tests

    @Test
    fun `should build correct photo URL with bucket and region`() {
        // Arrange
        val photoKey = "pets/user123/pet456/photo.jpg"
        val bucketName = "my-pet-bucket"
        val region = "us-east-1"

        whenever(s3Properties.bucketName).thenReturn(bucketName)
        whenever(s3Properties.region).thenReturn(region)

        // Act
        val result = adapter.buildPhotoUrl(photoKey)

        // Assert
        val expectedUrl = "https://$bucketName.s3.$region.amazonaws.com/$photoKey"
        assert(result == expectedUrl) { "Expected $expectedUrl but got $result" }
    }

    @Test
    fun `should build URL with different bucket name`() {
        // Arrange
        val photoKey = "pets/user789/pet012/image.png"
        val bucketName = "production-bucket"
        val region = "eu-west-1"

        whenever(s3Properties.bucketName).thenReturn(bucketName)
        whenever(s3Properties.region).thenReturn(region)

        // Act
        val result = adapter.buildPhotoUrl(photoKey)

        // Assert
        assert(result == "https://$bucketName.s3.$region.amazonaws.com/$photoKey")
    }

    @Test
    fun `should build URL with different region`() {
        // Arrange
        val photoKey = "pets/user999/pet888/test.jpg"
        val bucketName = "test-bucket"
        val region = "ap-south-1"

        whenever(s3Properties.bucketName).thenReturn(bucketName)
        whenever(s3Properties.region).thenReturn(region)

        // Act
        val result = adapter.buildPhotoUrl(photoKey)

        // Assert
        assert(result == "https://$bucketName.s3.$region.amazonaws.com/$photoKey")
    }

    @Test
    fun `should build URL with complex photo key`() {
        // Arrange
        val photoKey = "pets/user-abc-123/pet-xyz-789/uuid-12345.jpg"
        val bucketName = "complex-bucket"
        val region = "ca-central-1"

        whenever(s3Properties.bucketName).thenReturn(bucketName)
        whenever(s3Properties.region).thenReturn(region)

        // Act
        val result = adapter.buildPhotoUrl(photoKey)

        // Assert
        assert(result == "https://$bucketName.s3.$region.amazonaws.com/$photoKey")
    }
}
