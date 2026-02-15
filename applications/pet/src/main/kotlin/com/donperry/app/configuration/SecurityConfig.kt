package com.donperry.app.configuration

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.security.web.server.SecurityWebFilterChain

@Configuration
@EnableWebFluxSecurity
class SecurityConfig {

    @Bean
    fun securityWebFilterChain(http: ServerHttpSecurity): SecurityWebFilterChain {
        return http
            .authorizeExchange { exchanges ->
                exchanges
                    .pathMatchers("/management/**").permitAll()
                    .pathMatchers(HttpMethod.POST, "/api/pets").authenticated()
                    .anyExchange().permitAll()
            }
            .oauth2ResourceServer { oauth2 ->
                oauth2.jwt { }
            }
            .csrf { it.disable() }
            .build()
    }
}
