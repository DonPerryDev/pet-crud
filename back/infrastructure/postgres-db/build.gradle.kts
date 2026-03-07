object Versions {
    const val mockito = "5.8.0"
    const val mockitoKotlin = "5.2.1"
}

dependencies {
    implementation(project(":model"))
    implementation("org.springframework:spring-context")

    implementation("org.springframework.boot:spring-boot-starter-data-r2dbc")
    runtimeOnly("org.postgresql:postgresql")
    runtimeOnly("org.postgresql:r2dbc-postgresql")

    testImplementation("org.mockito:mockito-core:${Versions.mockito}")
    testImplementation("org.mockito.kotlin:mockito-kotlin:${Versions.mockitoKotlin}")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
}
