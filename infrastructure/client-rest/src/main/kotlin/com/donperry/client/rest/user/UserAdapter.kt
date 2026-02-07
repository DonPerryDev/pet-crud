package com.donperry.client.rest.user

import com.donperry.client.rest.user.dto.UserData
import com.donperry.client.rest.user.mapper.UserMapper
import com.donperry.model.user.User
import com.donperry.model.user.gateway.UserGateway
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono

@Component
class UserAdapter(
    private val webClient: WebClient,
) : UserGateway {
    override fun getByEmail(mail: String): Mono<User> =
        webClient
            .get()
            .uri("/disabled/v1/users/email/{email}", mail)
            .retrieve()
            .bodyToMono(UserData::class.java)
            .map { UserMapper.toUser(it) }
}
