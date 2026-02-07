package com.donperry.app.security

import com.donperry.rest.security.SecurityProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.reactive.CorsWebFilter
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource

@Configuration
class CorsGlobalConfig(
    private val securityProperties: SecurityProperties,
) {
    @Bean
    fun corsWebFilter(): CorsWebFilter {
        println(securityProperties.allowedOrigins)
        val corsConfig =
            CorsConfiguration().apply {
                allowedOrigins = securityProperties.allowedOrigins
                allowedMethods = listOf("GET", "POST", "PATCH", "PUT", "DELETE", "OPTIONS")
                allowedHeaders = listOf("*")
                allowCredentials = true
            }

        val source = UrlBasedCorsConfigurationSource()
        source.registerCorsConfiguration("/**", corsConfig)
        return CorsWebFilter(source)
    }
}
