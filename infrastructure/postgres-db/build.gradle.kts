dependencies {
    implementation(project(":model"))
    implementation("org.springframework:spring-context")

    testImplementation("org.mockito:mockito-core:5.8.0")
    testImplementation("org.mockito.kotlin:mockito-kotlin:5.2.1")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
}
