package com.donperry.storage.photo

import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import software.amazon.awssdk.services.s3.S3AsyncClient
import software.amazon.awssdk.services.s3.presigner.S3Presigner

@Configuration
@EnableConfigurationProperties(S3Properties::class)
class S3ClientConfig {

    @Bean
    fun s3AsyncClient(): S3AsyncClient = S3AsyncClient.create()

    @Bean
    fun s3Presigner(): S3Presigner = S3Presigner.create()
}
