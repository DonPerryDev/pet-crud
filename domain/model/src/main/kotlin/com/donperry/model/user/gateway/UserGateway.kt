package com.donperry.model.user.gateway

import com.donperry.model.user.User
import reactor.core.publisher.Mono

interface UserGateway {
    fun getByEmail(email: String): Mono<User>
}
