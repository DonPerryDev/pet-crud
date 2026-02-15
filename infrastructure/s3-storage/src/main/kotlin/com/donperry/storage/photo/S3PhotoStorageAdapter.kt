package com.donperry.storage.photo

import com.donperry.model.exception.PhotoUploadException
import com.donperry.model.pet.gateway.PhotoStorageGateway
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono
import software.amazon.awssdk.core.async.AsyncRequestBody
import software.amazon.awssdk.services.s3.S3AsyncClient
import software.amazon.awssdk.services.s3.model.PutObjectRequest
import java.util.logging.Logger

@Component
class S3PhotoStorageAdapter(
    private val s3AsyncClient: S3AsyncClient,
    private val s3Properties: S3Properties
) : PhotoStorageGateway {

    companion object {
        private val logger: Logger = Logger.getLogger(S3PhotoStorageAdapter::class.java.name)
    }

    override fun uploadPhoto(
        userId: String,
        petId: String,
        fileName: String,
        contentType: String,
        fileSize: Long,
        fileBytes: ByteArray
    ): Mono<String> {
        val key = "pets/$userId/$petId/$fileName"

        logger.info("[$petId] Uploading photo to S3: bucket=${s3Properties.bucketName}, key=$key")

        val putObjectRequest = PutObjectRequest.builder()
            .bucket(s3Properties.bucketName)
            .key(key)
            .contentType(contentType)
            .contentLength(fileSize)
            .build()

        return Mono.fromFuture(
            s3AsyncClient.putObject(putObjectRequest, AsyncRequestBody.fromBytes(fileBytes))
        )
        .map {
            val url = "https://${s3Properties.bucketName}.s3.${s3Properties.region}.amazonaws.com/$key"
            logger.info("[$petId] Photo uploaded successfully: $url")
            url
        }
        .onErrorMap { error ->
            logger.warning("[$petId] Failed to upload photo to S3: ${error.message}")
            PhotoUploadException("Failed to upload photo to S3", error)
        }
    }
}
