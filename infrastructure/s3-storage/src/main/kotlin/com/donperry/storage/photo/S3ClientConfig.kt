package com.donperry.storage.photo

import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.s3.S3AsyncClient
import software.amazon.awssdk.services.s3.presigner.S3Presigner
import java.net.URI

@Configuration
@EnableConfigurationProperties(S3Properties::class)
class S3ClientConfig {

    @Bean
    fun s3AsyncClient(s3Properties: S3Properties): S3AsyncClient {
        val credentialsProvider = createCredentialsProvider(s3Properties)

        return S3AsyncClient.builder()
            .region(Region.of(s3Properties.region))
            .credentialsProvider(credentialsProvider)
            .build()
    }

    @Bean
    fun s3Presigner(s3Properties: S3Properties): S3Presigner {
        val credentialsProvider = createCredentialsProvider(s3Properties)

        return S3Presigner.builder()
            .region(Region.of(s3Properties.region))
            .credentialsProvider(credentialsProvider)
            .build()
    }

    private fun createCredentialsProvider(s3Properties: S3Properties): AwsCredentialsProvider {
        return if (!s3Properties.accessKeyId.isNullOrBlank() && !s3Properties.secretAccessKey.isNullOrBlank()) {
            StaticCredentialsProvider.create(
                AwsBasicCredentials.create(s3Properties.accessKeyId, s3Properties.secretAccessKey)
            )
        } else {
            DefaultCredentialsProvider.create()
        }
    }
}
