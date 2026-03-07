object Versions {
    const val awsSdk = "2.20.0"
}

dependencies {
    implementation(project(":model"))
    implementation("org.springframework:spring-context")
    implementation("org.springframework.boot:spring-boot-starter")

    // AWS SDK v2
    implementation(platform("software.amazon.awssdk:bom:${Versions.awsSdk}"))
    implementation("software.amazon.awssdk:s3")
    implementation("software.amazon.awssdk:netty-nio-client")

    // Test dependencies
    testImplementation("org.mockito:mockito-core:5.8.0")
    testImplementation("org.mockito.kotlin:mockito-kotlin:5.2.1")
    testImplementation("io.projectreactor:reactor-test")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}
