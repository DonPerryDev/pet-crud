object Versions {
    const val mockito = "5.8.0"
    const val mockitoKotlin = "5.2.1"
}

dependencies {
    implementation(project(":model"))
    testImplementation("org.mockito:mockito-core:${Versions.mockito}")
    testImplementation("org.mockito.kotlin:mockito-kotlin:${Versions.mockitoKotlin}")
    testImplementation("io.projectreactor:reactor-test")
}
