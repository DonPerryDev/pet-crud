package com.donperry.rest.pet

import com.donperry.rest.pet.handler.PetHandler
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.MediaType
import org.springframework.web.reactive.function.server.RouterFunction
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.router

@Configuration
class PetRouter {

    @Bean
    fun petRoutes(petHandler: PetHandler): RouterFunction<ServerResponse> {
        return router {
            "/api/pets".nest {
                contentType(MediaType.MULTIPART_FORM_DATA).nest {
                    POST("", petHandler::registerPet)
                }
            }
        }
    }
}