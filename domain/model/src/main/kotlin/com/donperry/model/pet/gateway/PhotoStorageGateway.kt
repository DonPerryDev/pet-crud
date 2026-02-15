package com.donperry.model.pet.gateway

import com.donperry.model.pet.PhotoUploadData
import reactor.core.publisher.Mono

interface PhotoStorageGateway {
    fun uploadPhoto(userId: String, petId: String, photo: PhotoUploadData): Mono<String>
}
