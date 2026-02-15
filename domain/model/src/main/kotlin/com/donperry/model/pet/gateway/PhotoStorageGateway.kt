package com.donperry.model.pet.gateway

import reactor.core.publisher.Mono

interface PhotoStorageGateway {
    fun uploadPhoto(
        userId: String,
        petId: String,
        fileName: String,
        contentType: String,
        fileSize: Long,
        fileBytes: ByteArray
    ): Mono<String>
}
