package com.donperry.storage.photo

import com.donperry.model.pet.PresignedUploadUrl
import com.donperry.model.pet.gateway.PhotoStorageGateway
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono
import software.amazon.awssdk.services.s3.S3AsyncClient
import software.amazon.awssdk.services.s3.model.HeadObjectRequest
import software.amazon.awssdk.services.s3.model.NoSuchKeyException
import software.amazon.awssdk.services.s3.model.PutObjectRequest
import software.amazon.awssdk.services.s3.presigner.S3Presigner
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest
import java.time.Duration
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.UUID
import java.util.logging.Logger

@Component
class S3PhotoStorageAdapter(
    private val s3AsyncClient: S3AsyncClient,
    private val s3Presigner: S3Presigner,
    private val s3Properties: S3Properties
) : PhotoStorageGateway {

    companion object {
        private val logger: Logger = Logger.getLogger(S3PhotoStorageAdapter::class.java.name)
    }

    override fun generatePresignedUrl(userId: String, petId: String, contentType: String, expirationMinutes: Int): Mono<PresignedUploadUrl> {
        val fileExtension = when (contentType) {
            "image/jpeg" -> "jpg"
            "image/png" -> "png"
            else -> "bin"
        }

        val fileName = "${UUID.randomUUID()}.$fileExtension"
        val key = "pets/$userId/$petId/$fileName"

        logger.info("[$petId] Generating presigned URL for key: $key")

        return Mono.fromCallable {
            val putObjectRequest = PutObjectRequest.builder()
                .bucket(s3Properties.bucketName)
                .key(key)
                .contentType(contentType)
                .build()

            val presignRequest = PutObjectPresignRequest.builder()
                .signatureDuration(Duration.ofMinutes(expirationMinutes.toLong()))
                .putObjectRequest(putObjectRequest)
                .build()

            val presignedRequest = s3Presigner.presignPutObject(presignRequest)
            val expiresAt = Instant.now().plus(expirationMinutes.toLong(), ChronoUnit.MINUTES)

            PresignedUploadUrl(
                uploadUrl = presignedRequest.url().toString(),
                key = key,
                expiresAt = expiresAt
            )
        }
        .doOnNext { presignedUrl ->
            logger.info("[$petId] Presigned URL generated successfully, expires at: ${presignedUrl.expiresAt}")
        }
        .doOnError { error ->
            logger.warning("[$petId] Failed to generate presigned URL: ${error.message}")
        }
    }

    override fun verifyPhotoExists(photoKey: String): Mono<Boolean> {
        logger.fine("Verifying photo exists: $photoKey")

        val headObjectRequest = HeadObjectRequest.builder()
            .bucket(s3Properties.bucketName)
            .key(photoKey)
            .build()

        return Mono.fromFuture(s3AsyncClient.headObject(headObjectRequest))
            .map { true }
            .onErrorResume { error ->
                if (error is NoSuchKeyException) {
                    logger.warning("Photo not found in S3: $photoKey")
                    Mono.just(false)
                } else {
                    logger.warning("Error verifying photo existence: ${error.message}")
                    Mono.error(error)
                }
            }
    }

    override fun buildPhotoUrl(photoKey: String): String {
        return "https://${s3Properties.bucketName}.s3.${s3Properties.region}.amazonaws.com/$photoKey"
    }
}
