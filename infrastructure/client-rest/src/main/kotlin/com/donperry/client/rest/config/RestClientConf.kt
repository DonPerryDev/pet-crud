package com.donperry.client.rest.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.function.client.WebClient

@Configuration
class RestClientConf {
    @Value("\${app.user.host}")
    lateinit var userHost: String

    @Bean
    fun WebClient(): WebClient =
        WebClient
            .builder()
            .baseUrl(userHost)
            .build()
}
