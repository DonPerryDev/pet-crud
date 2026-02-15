package com.donperry.model.exception

sealed class DomainException(message: String, cause: Throwable? = null) : RuntimeException(message, cause)

class ValidationException(message: String) : DomainException(message)

class PetLimitExceededException(userId: String) :
    DomainException("User $userId has reached the maximum limit of 10 pets")

class PhotoUploadException(message: String, cause: Throwable? = null) : DomainException(message, cause)

class PhotoSizeExceededException(actualSize: Long, maxSize: Long) :
    DomainException("Photo size $actualSize bytes exceeds maximum allowed size of $maxSize bytes")

class UnauthorizedException(message: String) : DomainException(message)

class PetNotFoundException(petId: String) : DomainException("Pet with id $petId not found")

class PhotoNotFoundException(photoKey: String) : DomainException("Photo not found in storage: $photoKey")
