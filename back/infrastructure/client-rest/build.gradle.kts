object Versions {
    const val jackson = "2.18.3"
    const val sendgrid = "4.10.3"
    const val mockito = "5.8.0"
    const val mockitoKotlin = "5.2.1"
    const val mockWebServer = "4.12.0"
}

dependencies {
    implementation(project(":model"))
    implementation("org.springframework:spring-context")
    implementation("org.springframework.boot:spring-boot-starter-webflux")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:${Versions.jackson}")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:${Versions.jackson}")
    implementation("com.sendgrid:sendgrid-java:${Versions.sendgrid}")

    testImplementation("org.mockito:mockito-core:${Versions.mockito}")
    testImplementation("org.mockito.kotlin:mockito-kotlin:${Versions.mockitoKotlin}")
    testImplementation("io.projectreactor:reactor-test")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("com.squareup.okhttp3:mockwebserver:${Versions.mockWebServer}")
}
