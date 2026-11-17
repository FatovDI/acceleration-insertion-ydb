package com.example.ydbinsertion

import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource

abstract class AbstractYdbIntegrationTest {

    companion object {

        @JvmStatic
        @DynamicPropertySource
        fun ydbProperties(registry: DynamicPropertyRegistry) {
            registry.add("spring.datasource.url") { YdbTestContainer.jdbcUrl() }
            registry.add("spring.datasource.username") { "" }
            registry.add("spring.datasource.password") { "" }
            registry.add("spring.datasource.driver-class-name") { "tech.ydb.jdbc.YdbDriver" }
        }
    }
}
