package com.donperry.app.configuration

import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.FilterType

@Configuration
@ComponentScan(
    basePackages = ["com.donperry.usecase"],
    includeFilters = [
        ComponentScan.Filter(type = FilterType.REGEX, pattern = ["^.+UseCase$"]),
    ],
    useDefaultFilters = false,
)
class UseCasesConfig
