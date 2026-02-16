package com.donperry.rest.pet

import com.donperry.model.exception.UnauthorizedException
import com.donperry.rest.common.dto.ErrorResponse
import com.donperry.rest.pet.handler.PetHandler
import com.donperry.rest.security.JwtUtils
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.web.reactive.function.server.RouterFunction
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.router
import java.time.Instant

@Configuration
class PetRouter {

    @Bean
    fun petRoutes(petHandler: PetHandler): RouterFunction<ServerResponse> {
        return router {
            "/api/pets".nest {
                contentType(MediaType.APPLICATION_JSON).nest {
                    POST("", petHandler::registerPet)
                    PUT("/{petId}", petHandler::updatePet)
                    POST("/{petId}/avatar/presign", petHandler::generatePresignedUrl)
                    POST("/{petId}/avatar/confirm", petHandler::confirmAvatarUpload)
                }
            }
        }.filter{ request, next ->
            try {
                val authHeader = request.headers().firstHeader("Authorization")
                val userId = JwtUtils.extractUserId(authHeader)
                request.attributes()["userId"] = userId
                next.handle(request)
            } catch (e: UnauthorizedException) {
                ServerResponse.status(HttpStatus.UNAUTHORIZED)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(
                        ErrorResponse(
                            error = "UNAUTHORIZED",
                            message = e.message ?: "Unauthorized",
                            timestamp = Instant.now().toString()
                        )
                    )
            }
        }
    }
}
