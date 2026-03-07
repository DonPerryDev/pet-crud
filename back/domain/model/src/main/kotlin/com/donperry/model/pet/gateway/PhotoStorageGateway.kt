package com.donperry.model.pet.gateway

import com.donperry.model.pet.PresignedUploadUrl
import reactor.core.publisher.Mono

interface PhotoStorageGateway {
    fun generatePresignedUrl(userId: String, petId: String, contentType: String, expirationMinutes: Int): Mono<PresignedUploadUrl>
    fun verifyPhotoExists(photoKey: String): Mono<Boolean>
    fun buildPhotoKey(userId: String, petId: String, contentType: String): String
    fun buildPhotoUrl(photoKey: String): String
}
