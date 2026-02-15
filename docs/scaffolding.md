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

data class {Entity}(
    val id: String? = null,
    // fields
)
```

## 2. Gateway Interface

**File:** `domain/model/src/main/kotlin/com/donperry/model/{entity}/gateway/{Entity}PersistenceGateway.kt`

```kotlin
package com.donperry.model.{entity}.gateway

import com.donperry.model.{entity}.{Entity}
import reactor.core.publisher.Mono

interface {Entity}PersistenceGateway {
    fun save({entity}: {Entity}): Mono<{Entity}>
    fun findById(id: String): Mono<{Entity}>
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
import org.springframework.data.relational.core.mapping.Table

@Table("{entity}s")
data class {Entity}Data(
    @Id val id: String? = null,
    // fields matching database columns
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

## 12. Wiring Config (if new use case)

Use cases matching `^.+UseCase$` are auto-scanned by `UseCasesConfig.kt`. No additional wiring needed unless the use case class name doesn't end with `UseCase`.
