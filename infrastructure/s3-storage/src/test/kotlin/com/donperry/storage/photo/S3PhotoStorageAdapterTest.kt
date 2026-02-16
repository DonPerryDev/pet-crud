package com.donperry.storage.photo

import com.donperry.model.pet.PresignedUploadUrl
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.MockedConstruction
import org.mockito.Mockito.mockConstruction
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.junit.jupiter.MockitoSettings
import org.mockito.quality.Strictness
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import reactor.test.StepVerifier
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.regions.providers.DefaultAwsRegionProviderChain
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

    private lateinit var s3Properties: S3Properties

    private lateinit var adapter: S3PhotoStorageAdapter

    private lateinit var regionProviderMock: MockedConstruction<DefaultAwsRegionProviderChain>

    @BeforeEach
    fun setUp() {
        // Mock DefaultAwsRegionProviderChain to return a test region
        regionProviderMock = mockConstruction(DefaultAwsRegionProviderChain::class.java) { mock, _ ->
            whenever(mock.region).thenReturn(Region.US_EAST_1)
        }

        // Use real S3Properties instance instead of mock
        s3Properties = S3Properties(bucketName = "test-bucket")

        adapter = S3PhotoStorageAdapter(s3AsyncClient, s3Presigner, s3Properties)
    }

    @AfterEach
    fun tearDown() {
        regionProviderMock.close()
    }

    // generatePresignedUrl tests

    @Test
    fun `should generate presigned URL with correct uploadUrl and key pattern`() {
        // Arrange
        val userId = "user123"
        val petId = "pet456"
        val contentType = "image/jpeg"
        val expirationMinutes = 15

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

        val mockPresignedRequest = mock<PresignedPutObjectRequest>()
        whenever(mockPresignedRequest.url()).thenReturn(URL("https://bucket.s3.amazonaws.com/signed"))
        whenever(s3Presigner.presignPutObject(any<PutObjectPresignRequest>())).thenReturn(mockPresignedRequest)

        // Act & Assert
        StepVerifier.create(adapter.generatePresignedUrl(userId, petId, contentType, expirationMinutes))
            .assertNext { result ->
                assert(result.key.endsWith(".jpg"))
                assert(result.key.matches(Regex("pets/$userId/$petId/avatar\\.jpg")))
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

        val mockPresignedRequest = mock<PresignedPutObjectRequest>()
        whenever(mockPresignedRequest.url()).thenReturn(URL("https://bucket.s3.amazonaws.com/signed"))
        whenever(s3Presigner.presignPutObject(any<PutObjectPresignRequest>())).thenReturn(mockPresignedRequest)

        // Act & Assert
        StepVerifier.create(adapter.generatePresignedUrl(userId, petId, contentType, expirationMinutes))
            .assertNext { result ->
                assert(result.key.endsWith(".png"))
                assert(result.key.matches(Regex("pets/$userId/$petId/avatar\\.png")))
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

        val mockPresignedRequest = mock<PresignedPutObjectRequest>()
        whenever(mockPresignedRequest.url()).thenReturn(URL("https://bucket.s3.amazonaws.com/signed"))
        whenever(s3Presigner.presignPutObject(any<PutObjectPresignRequest>())).thenReturn(mockPresignedRequest)

        // Act & Assert
        StepVerifier.create(adapter.generatePresignedUrl(userId, petId, contentType, expirationMinutes))
            .assertNext { result ->
                assert(result.key.endsWith(".bin"))
                assert(result.key.matches(Regex("pets/$userId/$petId/avatar\\.bin")))
            }
            .verifyComplete()
    }

    @Test
    fun `should generate key following pattern pets-userId-petId-avatar-extension`() {
        // Arrange
        val userId = "test-user"
        val petId = "test-pet"
        val contentType = "image/jpeg"
        val expirationMinutes = 15

        val mockPresignedRequest = mock<PresignedPutObjectRequest>()
        whenever(mockPresignedRequest.url()).thenReturn(URL("https://bucket.s3.amazonaws.com/signed"))
        whenever(s3Presigner.presignPutObject(any<PutObjectPresignRequest>())).thenReturn(mockPresignedRequest)

        // Act & Assert
        StepVerifier.create(adapter.generatePresignedUrl(userId, petId, contentType, expirationMinutes))
            .assertNext { result ->
                val keyPattern = Regex("pets/$userId/$petId/avatar\\.jpg")
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
    fun `should build correct photo URL with bucket name and photo key`() {
        // Arrange
        val photoKey = "pets/user123/pet456/photo.jpg"

        // Act
        val result = adapter.buildPhotoUrl(photoKey)

        // Assert
        // Region is resolved from DefaultAwsRegionProviderChain (mocked to us-east-1)
        assert(result.contains("test-bucket")) { "URL should contain bucket name: $result" }
        assert(result.contains(photoKey)) { "URL should contain photo key: $result" }
        assert(result.startsWith("https://")) { "URL should use HTTPS: $result" }
        assert(result.contains(".s3.")) { "URL should contain S3 domain: $result" }
        assert(result.contains(".amazonaws.com/")) { "URL should contain amazonaws.com: $result" }
    }

    @Test
    fun `should build URL with correct S3 format`() {
        // Arrange
        val photoKey = "pets/user789/pet012/image.png"

        // Act
        val result = adapter.buildPhotoUrl(photoKey)

        // Assert
        val urlPattern = Regex("https://test-bucket\\.s3\\.[a-z0-9-]+\\.amazonaws\\.com/pets/user789/pet012/image\\.png")
        assert(result.matches(urlPattern)) { "URL $result does not match expected S3 URL format" }
    }

    @Test
    fun `should build URL using region from DefaultAwsRegionProviderChain`() {
        // Arrange
        val photoKey = "pets/user999/pet888/test.jpg"

        // Act
        val result = adapter.buildPhotoUrl(photoKey)

        // Assert
        // The region should be us-east-1 from the mocked DefaultAwsRegionProviderChain
        val expectedUrl = "https://test-bucket.s3.us-east-1.amazonaws.com/$photoKey"
        assert(result == expectedUrl) { "Expected $expectedUrl but got $result" }
    }

    @Test
    fun `should build URL with complex photo key`() {
        // Arrange
        val photoKey = "pets/user-abc-123/pet-xyz-789/uuid-12345.jpg"

        // Act
        val result = adapter.buildPhotoUrl(photoKey)

        // Assert
        assert(result == "https://test-bucket.s3.us-east-1.amazonaws.com/$photoKey") {
            "URL format incorrect: $result"
        }
    }

    // buildPhotoKey tests

    @Test
    fun `should build photo key with jpg extension for image-jpeg content type`() {
        // Arrange
        val userId = "user123"
        val petId = "pet456"
        val contentType = "image/jpeg"

        // Act
        val result = adapter.buildPhotoKey(userId, petId, contentType)

        // Assert
        assert(result == "pets/$userId/$petId/avatar.jpg") {
            "Expected pets/$userId/$petId/avatar.jpg but got $result"
        }
    }

    @Test
    fun `should build photo key with png extension for image-png content type`() {
        // Arrange
        val userId = "user789"
        val petId = "pet012"
        val contentType = "image/png"

        // Act
        val result = adapter.buildPhotoKey(userId, petId, contentType)

        // Assert
        assert(result == "pets/$userId/$petId/avatar.png") {
            "Expected pets/$userId/$petId/avatar.png but got $result"
        }
    }

    @Test
    fun `should build photo key with bin extension for unknown content type`() {
        // Arrange
        val userId = "user555"
        val petId = "pet666"
        val contentType = "application/octet-stream"

        // Act
        val result = adapter.buildPhotoKey(userId, petId, contentType)

        // Assert
        assert(result == "pets/$userId/$petId/avatar.bin") {
            "Expected pets/$userId/$petId/avatar.bin but got $result"
        }
    }

    @Test
    fun `should build photo key with bin extension for empty content type`() {
        // Arrange
        val userId = "userABC"
        val petId = "petXYZ"
        val contentType = ""

        // Act
        val result = adapter.buildPhotoKey(userId, petId, contentType)

        // Assert
        assert(result == "pets/$userId/$petId/avatar.bin") {
            "Expected pets/$userId/$petId/avatar.bin but got $result"
        }
    }

    @Test
    fun `should build photo key following pets-userId-petId-avatar-extension pattern`() {
        // Arrange
        val userId = "test-user-123"
        val petId = "test-pet-456"
        val contentType = "image/jpeg"

        // Act
        val result = adapter.buildPhotoKey(userId, petId, contentType)

        // Assert
        val keyPattern = Regex("pets/test-user-123/test-pet-456/avatar\\.jpg")
        assert(result.matches(keyPattern)) {
            "Key $result does not match expected pattern"
        }
    }
}
