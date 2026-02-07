package com.donperry.rest.security

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration
@ConfigurationProperties("app.security")
class SecurityProperties {
    var allowedOrigins: List<String> = emptyList()
}
