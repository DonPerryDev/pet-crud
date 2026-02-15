package com.donperry.rest.security

import com.donperry.rest.security.SecurityProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.reactive.CorsWebFilter
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource
import java.util.logging.Logger

@Configuration
class CorsGlobalConfig(
    private val securityProperties: SecurityProperties,
) {
    companion object {
        private val logger: Logger = Logger.getLogger(CorsGlobalConfig::class.java.name)
    }

    @Bean
    fun corsWebFilter(): CorsWebFilter {
        logger.info("Configuring CORS with allowed origins: ${securityProperties.allowedOrigins}")
        val corsConfig =
            CorsConfiguration().apply {
                allowedOrigins = securityProperties.allowedOrigins
                allowedMethods = listOf("GET", "POST", "PATCH", "PUT", "DELETE", "OPTIONS")
                allowedHeaders = listOf("Content-Type", "Authorization")
                allowCredentials = true
            }

        val source = UrlBasedCorsConfigurationSource()
        source.registerCorsConfiguration("/**", corsConfig)
        return CorsWebFilter(source)
    }
}
