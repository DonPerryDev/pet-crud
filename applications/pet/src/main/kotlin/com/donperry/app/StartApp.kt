package com.donperry.app

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication(scanBasePackages = ["com.donperry"])
class PetApplication

fun main(args: Array<String>) {
    runApplication<PetApplication>(*args)
}
