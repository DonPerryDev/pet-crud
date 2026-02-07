pluginManagement {
    repositories {
        gradlePluginPortal()
    }
}

rootProject.name = "pet-app"

include(":pet")
project(":pet").projectDir = file("./applications/pet")

include(":model")
project(":model").projectDir = file("./domain/model")

include(":usecase")
project(":usecase").projectDir = file("./domain/usecase")

include(":rest")
project(":rest").projectDir = file("./infrastructure/entrypoint-rest")

include(":persistence")
project(":persistence").projectDir = file("./infrastructure/postgres-db")

include(":client-rest")
project(":client-rest").projectDir = file("./infrastructure/client-rest")
