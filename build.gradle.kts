plugins {
    kotlin("jvm") version "2.1.10"
    kotlin("plugin.spring") version "2.1.10"
    id("org.springframework.boot") version "3.4.3"
    id("io.spring.dependency-management") version "1.1.7"
    jacoco
}

group = "com.donperry"
version = "1.7.1"

subprojects {

    apply(plugin = "kotlin")
    apply(plugin = "io.spring.dependency-management")
    apply(plugin = "org.springframework.boot")
    apply(plugin = "kotlin-spring")
    apply(plugin = "jacoco")

    dependencies {
        implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
        implementation("io.projectreactor.kotlin:reactor-kotlin-extensions")
        implementation("org.springframework.boot:spring-boot-starter-actuator")
        testImplementation("org.springframework.boot:spring-boot-starter-test")
        testImplementation("io.projectreactor:reactor-test")
        testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
        testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    }

    plugins.withId("org.springframework.boot") {
        tasks.bootJar {
            enabled = project.name == "pet"
        }
        tasks.jar {
            enabled = project.name != "pet"
        }
    }
}

allprojects {
    tasks.withType<Test> {
        useJUnitPlatform()
    }
    repositories {
        mavenCentral()
    }

    java {
        toolchain {
            languageVersion = JavaLanguageVersion.of(21)
        }
    }

    kotlin {
        compilerOptions {
            freeCompilerArgs.addAll("-Xjsr305=strict")
        }
    }

    jacoco {
        toolVersion = "0.8.12"
    }

    tasks.jacocoTestReport {
        dependsOn(tasks.test)
        reports {
            xml.required.set(true)
            html.required.set(true)
        }
    }

    tasks.test {
        finalizedBy(tasks.jacocoTestReport)
    }
}

tasks.bootJar {
    enabled = false
}

val jacocoMergedReport =
    tasks.register<JacocoReport>("jacocoMergedReport") {
        dependsOn(subprojects.map { it.tasks.named("test") })

        description = "Generates a merged JaCoCo test report for all subprojects"
        group = "reporting"

        executionData.setFrom(
            subprojects.map {
                fileTree("${it.layout.buildDirectory.get()}/jacoco").include("**/*.exec")
            },
        )

        sourceDirectories.setFrom(
            subprojects.map {
                it.file("src/main/kotlin")
            },
        )

        classDirectories.setFrom(
            subprojects.map {
                fileTree("${it.layout.buildDirectory.get()}/classes/kotlin/main")
            },
        )

        reports {
            xml.required.set(true)
            html.required.set(true)
            xml.outputLocation.set(file("${layout.buildDirectory.get()}/reports/jacoco/jacocoMergedReport/jacocoMergedReport.xml"))
            html.outputLocation.set(file("${layout.buildDirectory.get()}/reports/jacoco/jacocoMergedReport/html"))
        }
    }

tasks.register("testWithMergedCoverage") {
    dependsOn("test", jacocoMergedReport)
    group = "verification"
    description = "Runs all tests and generates a merged JaCoCo coverage report"
}

tasks.named("build") {
    finalizedBy(jacocoMergedReport)
}
