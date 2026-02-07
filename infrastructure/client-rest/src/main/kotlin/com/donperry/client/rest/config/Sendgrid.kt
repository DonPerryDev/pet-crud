package com.donperry.client.rest.config

import com.sendgrid.SendGrid
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class Sendgrid {
    @Value("\${app.sendgrid.api-key}")
    lateinit var sendGridAPIKey: String

    @Bean
    fun sendGrid(): SendGrid = SendGrid(sendGridAPIKey)
}
