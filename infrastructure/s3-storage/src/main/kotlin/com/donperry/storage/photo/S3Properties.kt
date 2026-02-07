package com.donperry.storage.photo

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "aws.s3")
data class S3Properties(
    val bucketName: String
)
