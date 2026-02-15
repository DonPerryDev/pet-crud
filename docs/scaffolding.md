# Scaffolding Templates

Code templates for creating new features. Replace placeholders:
- `{Entity}` → PascalCase (e.g., `Pet`)
- `{entity}` → camelCase (e.g., `pet`)
- `{Action}` → PascalCase verb (e.g., `Register`)
- `{action}` → camelCase verb (e.g., `register`)

## 1. Domain Model

**File:** `domain/model/src/main/kotlin/com/donperry/model/{entity}/{Entity}.kt`

```kotlin
package com.donperry.model.{entity}

import java.time.LocalDate

data class {Entity}(
    val id: String? = null,
    val name: String,
    // Add other required fields
    val createdDate: LocalDate = LocalDate.now()
)
```

**Example (Pet):**
```kotlin
data class Pet(
    val id: String? = null,
    val name: String,
    val species: Species,
    val breed: String?,
    val age: Int,
    val birthdate: LocalDate? = null,
    val weight: BigDecimal? = null,
    val nickname: String? = null,
    val owner: String,
    val registrationDate: LocalDate,
    val photoUrl: String? = null
)
```

## 2. Gateway Interface

**File:** `domain/model/src/main/kotlin/com/donperry/model/{entity}/gateway/{Entity}PersistenceGateway.kt`

```kotlin
package com.donperry.model.{entity}.gateway

import com.donperry.model.{entity}.{Entity}
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

interface {Entity}PersistenceGateway {
    fun save({entity}: {Entity}): Mono<{Entity}>
    fun findById(id: String): Mono<{Entity}>
    fun findAll(): Flux<{Entity}>
    fun deleteById(id: String): Mono<Void>
}
```

**Example (PetPersistenceGateway):**
```kotlin
interface PetPersistenceGateway {
    fun save(pet: Pet): Mono<Pet>
    fun findById(id: String): Mono<Pet>
    fun countByOwner(owner: String): Mono<Long>
}
```

**For S3 Storage:**
**File:** `domain/model/src/main/kotlin/com/donperry/model/{entity}/gateway/{Type}StorageGateway.kt`

```kotlin
package com.donperry.model.{entity}.gateway

import com.donperry.model.{entity}.PresignedUploadUrl
import reactor.core.publisher.Mono

interface PhotoStorageGateway {
    fun generatePresignedUrl(userId: String, petId: String, contentType: String, expirationMinutes: Int): Mono<PresignedUploadUrl>
    fun verifyPhotoExists(photoKey: String): Mono<Boolean>
    fun buildPhotoUrl(photoKey: String): String
}
```

## 3. Use Case

**File:** `domain/usecase/src/main/kotlin/com/donperry/usecase/{entity}/{Action}{Entity}UseCase.kt`

```kotlin
package com.donperry.usecase.{entity}

import com.donperry.model.{entity}.{Entity}
import com.donperry.model.{entity}.gateway.{Entity}PersistenceGateway
import reactor.core.publisher.Mono
import java.util.logging.Logger

class {Action}{Entity}UseCase(
    private val {entity}PersistenceGateway: {Entity}PersistenceGateway
) {
    companion object {
        private val logger: Logger = Logger.getLogger({Action}{Entity}UseCase::class.java.name)
    }

    fun execute(/* params */): Mono<{Entity}> {
        logger.info("Starting {action} {entity} process")
        // Build domain entity, call gateway
        return {entity}PersistenceGateway.save({entity})
            .doOnNext { logger.info("[${it.id}] {Entity} {action}ed successfully") }
            .doOnError { logger.warning("{Entity} {action} failed: ${it.message}") }
    }
}
```

## 4. Request DTO

**File:** `infrastructure/entrypoint-rest/src/main/kotlin/com/donperry/rest/{entity}/dto/{Action}{Entity}Request.kt`

```kotlin
package com.donperry.rest.{entity}.dto

data class {Action}{Entity}Request(
    // fields matching API contract
)
```

## 5. Response DTO

**File:** `infrastructure/entrypoint-rest/src/main/kotlin/com/donperry/rest/{entity}/dto/{Entity}Response.kt`

```kotlin
package com.donperry.rest.{entity}.dto

data class {Entity}Response(
    val id: String,
    // fields
)
```

## 6. Handler

**File:** `infrastructure/entrypoint-rest/src/main/kotlin/com/donperry/rest/{entity}/handler/{Entity}Handler.kt`

```kotlin
package com.donperry.rest.{entity}.handler

import com.donperry.rest.{entity}.dto.{Action}{Entity}Request
import com.donperry.usecase.{entity}.{Action}{Entity}UseCase
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import reactor.core.publisher.Mono
import java.util.logging.Logger

@Component
class {Entity}Handler(
    private val {action}{Entity}UseCase: {Action}{Entity}UseCase
) {
    companion object {
        private val logger: Logger = Logger.getLogger({Entity}Handler::class.java.name)
    }

    fun {action}{Entity}(request: ServerRequest): Mono<ServerResponse> {
        logger.info("Received {action} {entity} request")
        return request
            .bodyToMono({Action}{Entity}Request::class.java)
            .flatMap { req ->
                {action}{Entity}UseCase.execute(/* map req fields */)
            }
            .flatMap { ServerResponse.ok().bodyValue(it) }
            .onErrorResume { throwable ->
                logger.warning("Error during {action} {entity}: ${throwable.message}")
                ServerResponse.badRequest().build()
            }
    }
}
```

## 7. Router

**File:** `infrastructure/entrypoint-rest/src/main/kotlin/com/donperry/rest/{entity}/{Entity}Router.kt`

```kotlin
package com.donperry.rest.{entity}

import com.donperry.rest.{entity}.handler.{Entity}Handler
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.MediaType
import org.springframework.web.reactive.function.server.RouterFunction
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.router

@Configuration
class {Entity}Router {

    @Bean
    fun {entity}Routes({entity}Handler: {Entity}Handler): RouterFunction<ServerResponse> {
        return router {
            "/api/{entity}s".nest {
                accept(MediaType.APPLICATION_JSON).nest {
                    POST("", {entity}Handler::{action}{Entity})
                    GET("/{id}", {entity}Handler::get{Entity})
                }
            }
        }
    }
}
```

## 8. Persistence Entity

**File:** `infrastructure/postgres-db/src/main/kotlin/com/donperry/persistence/{entity}/entities/{Entity}Data.kt`

```kotlin
package com.donperry.persistence.{entity}.entities

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Column
import org.springframework.data.relational.core.mapping.Table
import java.time.LocalDate
import java.util.UUID

@Table(name = "{entity}s")
data class {Entity}Data(
    @Id
    val id: UUID? = null,
    @Column("name")
    val name: String,
    // Add @Column for each field matching snake_case DB columns
    @Column("created_date")
    val createdDate: LocalDate
)
```

**Example (PetData):**
```kotlin
@Table(name = "pets")
data class PetData(
    @Id
    val id: UUID? = null,
    @Column("name")
    val name: String,
    @Column("species")
    val species: String,
    @Column("breed")
    val breed: String? = null,
    @Column("age")
    val age: Int,
    @Column("birthdate")
    val birthdate: LocalDate? = null,
    @Column("weight")
    val weight: BigDecimal? = null,
    @Column("nickname")
    val nickname: String? = null,
    @Column("owner")
    val owner: String,
    @Column("registration_date")
    val registrationDate: LocalDate,
    @Column("photo_url")
    val photoUrl: String? = null
)
```

## 9. Repository

**File:** `infrastructure/postgres-db/src/main/kotlin/com/donperry/persistence/{entity}/{Entity}Repository.kt`

```kotlin
package com.donperry.persistence.{entity}

import com.donperry.persistence.{entity}.entities.{Entity}Data
import org.springframework.data.repository.reactive.ReactiveCrudRepository

interface {Entity}Repository : ReactiveCrudRepository<{Entity}Data, String>
```

## 10. Mapper

**File:** `infrastructure/postgres-db/src/main/kotlin/com/donperry/persistence/{entity}/mapper/{Entity}Mapper.kt`

```kotlin
package com.donperry.persistence.{entity}.mapper

import com.donperry.model.{entity}.{Entity}
import com.donperry.persistence.{entity}.entities.{Entity}Data

object {Entity}Mapper {
    fun toEntity(model: {Entity}): {Entity}Data =
        {Entity}Data(
            id = model.id,
            // map fields
        )

    fun toModel(entity: {Entity}Data): {Entity} =
        {Entity}(
            id = entity.id,
            // map fields
        )
}
```

## 11. Persistence Adapter

**File:** `infrastructure/postgres-db/src/main/kotlin/com/donperry/persistence/{entity}/{Entity}PersistenceAdapter.kt`

```kotlin
package com.donperry.persistence.{entity}

import com.donperry.model.{entity}.{Entity}
import com.donperry.model.{entity}.gateway.{Entity}PersistenceGateway
import com.donperry.persistence.{entity}.mapper.{Entity}Mapper
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono

@Service
class {Entity}PersistenceAdapter(
    private val {entity}Repository: {Entity}Repository
) : {Entity}PersistenceGateway {

    override fun save({entity}: {Entity}): Mono<{Entity}> =
        {entity}Repository.save({Entity}Mapper.toEntity({entity}))
            .map { {Entity}Mapper.toModel(it) }

    override fun findById(id: String): Mono<{Entity}> =
        {entity}Repository.findById(id)
            .map { {Entity}Mapper.toModel(it) }
}
```

## 12. Domain Exceptions

**File:** `domain/model/src/main/kotlin/com/donperry/model/exception/DomainException.kt`

```kotlin
package com.donperry.model.exception

sealed class DomainException(message: String, cause: Throwable? = null) : RuntimeException(message, cause)

class ValidationException(message: String) : DomainException(message)

class {Entity}NotFoundException(id: String) : DomainException("{Entity} not found: $id")

class {Operation}Exception(message: String, cause: Throwable? = null) : DomainException(message, cause)
```

**Example (Pet exceptions):**
```kotlin
class PetLimitExceededException(userId: String) :
    DomainException("User $userId has reached the maximum limit of 10 pets")

class PhotoSizeExceededException(actualSize: Long, maxSize: Long) :
    DomainException("Photo size $actualSize bytes exceeds maximum allowed size of $maxSize bytes")

class PhotoUploadException(message: String, cause: Throwable? = null) :
    DomainException(message, cause)
```

## 13. Error Response DTO

**File:** `infrastructure/entrypoint-rest/src/main/kotlin/com/donperry/rest/common/dto/ErrorResponse.kt`

```kotlin
package com.donperry.rest.common.dto

data class ErrorResponse(
    val error: String,
    val message: String,
    val timestamp: String
)
```

**Handler error mapping pattern:**
```kotlin
private fun handleError(throwable: Throwable): Mono<ServerResponse> {
    return when (throwable) {
        is ValidationException -> buildErrorResponse(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", throwable)
        is {Entity}NotFoundException -> buildErrorResponse(HttpStatus.NOT_FOUND, "NOT_FOUND", throwable)
        else -> buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_ERROR", throwable)
    }
}

private fun buildErrorResponse(status: HttpStatus, error: String, throwable: Throwable): Mono<ServerResponse> {
    val errorResponse = ErrorResponse(
        error = error,
        message = throwable.message ?: status.reasonPhrase,
        timestamp = Instant.now().toString()
    )
    return ServerResponse.status(status)
        .contentType(MediaType.APPLICATION_JSON)
        .bodyValue(errorResponse)
}
```

## 14. Wiring Config

Use cases matching `^.+UseCase$` are auto-scanned by `UseCasesConfig.kt`. No additional wiring needed unless the use case class name doesn't end with `UseCase`.
