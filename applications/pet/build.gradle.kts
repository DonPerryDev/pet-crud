apply(plugin = "org.springframework.boot")

dependencies {
    implementation(project(":model"))
    implementation(project(":rest"))
    implementation(project(":usecase"))
    implementation(project(":persistence"))
    implementation("org.springframework.boot:spring-boot-starter-webflux")
    implementation("org.springframework.boot:spring-boot-starter")
    configurations {
        all {
            exclude(group = "org.springframework.boot", module = "spring-boot-starter-tomcat")
        }
    }
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("io.projectreactor.kotlin:reactor-kotlin-extensions")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor")
    implementation("org.springframework:spring-context")

    implementation("org.springframework.boot:spring-boot-starter-data-r2dbc")
}

tasks.register<Copy>("explodedJar") {
    description = "Extracts the JAR contents into an exploded directory"
    group = "build"
    with(tasks.jar.get())
    into(layout.buildDirectory.dir("exploded"))
}

tasks.bootJar {
    archiveFileName.set("${project.parent?.name}.${archiveExtension.get()}")
    mainClass.set("com.donperry.app.StartAppKt")
}
