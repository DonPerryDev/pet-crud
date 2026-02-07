package com.donperry.rest.pet.handler

import com.donperry.rest.pet.dto.RegisterPetRequest
import com.donperry.usecase.pet.RegisterPetUseCase
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import reactor.core.publisher.Mono
import java.util.logging.Logger

@Component
class PetHandler(
    private val registerPetUseCase: RegisterPetUseCase
) {
    companion object {
        private val logger: Logger = Logger.getLogger(PetHandler::class.java.name)
    }

    fun registerPet(request: ServerRequest): Mono<ServerResponse> {
        return request
            .bodyToMono(RegisterPetRequest::class.java)
            .map { registerPetUseCase.execute(
                it.name,
                it.species,
                it.breed,
                it.age,
                it.owner
            ) }
            .flatMap { ServerResponse.ok().build() }
            .onErrorResume {
                logger.warning("Error on readNotification: ${it.message}")
                ServerResponse.badRequest().build()
            }
    }

}