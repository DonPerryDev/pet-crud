package com.donperry.client.rest.user

import com.donperry.client.rest.user.dto.UserData
import com.donperry.client.rest.user.mapper.UserMapper
import com.donperry.model.user.User
import com.donperry.model.user.gateway.UserGateway
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono
import java.util.logging.Logger

@Component
class UserAdapter(
    private val webClient: WebClient,
) : UserGateway {

    companion object {
        private val logger: Logger = Logger.getLogger(UserAdapter::class.java.name)
    }

    override fun getByEmail(mail: String): Mono<User> {
        logger.info("Fetching user by email from external service")
        return webClient
            .get()
            .uri("/api/v1/users/email/{email}", mail)
            .retrieve()
            .bodyToMono(UserData::class.java)
            .map { UserMapper.toUser(it) }
            .doOnNext { user ->
                logger.info("[${user.id}] User retrieved successfully")
            }
            .doOnError { error ->
                logger.warning("Failed to retrieve user from external service: ${error.message}")
            }
    }
}
