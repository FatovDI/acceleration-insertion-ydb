package com.example.ydbinsertion

import tech.ydb.test.integration.YdbEnvironment
import tech.ydb.test.integration.docker.YdbDockerContainer
import tech.ydb.test.integration.utils.PortsGenerator

object YdbTestContainer {

    private const val DATABASE = "local"

    private val container: YdbDockerContainer by lazy {
        System.setProperty("api.version", "1.44")
        YdbDockerContainer(YdbEnvironment(), PortsGenerator()).apply { init() }
    }

    @Synchronized
    fun startIfNeeded() {
        if (!container.isRunning) {
            container.start()
        }
    }

    fun jdbcUrl(): String {
        startIfNeeded()
        val endpoint = container.nonSecureEndpoint()
        return "jdbc:ydb:grpc://${endpoint.hostAndPort}/?database=$DATABASE"
    }
}
