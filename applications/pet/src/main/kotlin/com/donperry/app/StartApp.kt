package com.donperry.app

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.data.r2dbc.repository.config.EnableR2dbcRepositories

@SpringBootApplication(scanBasePackages = ["com.donperry"])
@EnableR2dbcRepositories(basePackages = ["com.donperry"])
class PetApplication

fun main(args: Array<String>) {
    runApplication<PetApplication>(*args)
}
