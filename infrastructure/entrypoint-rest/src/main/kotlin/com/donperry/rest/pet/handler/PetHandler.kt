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
        logger.info("Received pet registration request")
        
        return request
            .bodyToMono(RegisterPetRequest::class.java)
            .doOnNext { 
                logger.fine("Processing pet registration for: name=${it.name}, species=${it.species}, breed=${it.breed}, age=${it.age}, owner=${it.owner}")
            }
            .flatMap {
                logger.info("Executing pet registration use case for pet: ${it.name}")
                registerPetUseCase.execute(
                    it.name,
                    it.species,
                    it.breed,
                    it.age,
                    it.owner
                )
            }
            .doOnNext { pet ->
                logger.info("Pet registered successfully with ID: ${pet.id}")
            }
            .flatMap { 
                logger.info("Returning successful response for pet registration")
                ServerResponse.ok().build() 
            }
            .onErrorResume { throwable ->
                logger.warning("Error during pet registration: ${throwable.message}")
                logger.fine("Error details: ${throwable.stackTraceToString()}")
                ServerResponse.badRequest().build()
            }
    }

}