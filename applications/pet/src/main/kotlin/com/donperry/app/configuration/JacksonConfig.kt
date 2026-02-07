package com.donperry.app.configuration

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer
import com.fasterxml.jackson.module.kotlin.kotlinModule
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Configuration
class JacksonConfig {
    @Bean
    fun objectMapper(): ObjectMapper {
        val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
        val customModule = SimpleModule()
        customModule.addDeserializer(LocalDateTime::class.java, LocalDateTimeDeserializer(dateFormatter))
        customModule.addSerializer(LocalDateTime::class.java, LocalDateTimeSerializer(dateFormatter))

        return ObjectMapper()
            .registerModule(kotlinModule())
            .registerModule(JavaTimeModule())
            .registerModule(customModule)
    }
}
