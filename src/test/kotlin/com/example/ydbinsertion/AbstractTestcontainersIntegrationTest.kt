package com.example.ydbinsertion

import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.util.TestPropertyValues
import org.springframework.context.ApplicationContextInitializer
import org.springframework.context.ConfigurableApplicationContext
import org.testcontainers.junit.jupiter.Container
import tech.ydb.test.integration.YdbEnvironment
import tech.ydb.test.integration.docker.YdbDockerContainer
import tech.ydb.test.integration.utils.PortsGenerator

@SpringBootTest
//@RunWith(SpringRunner::class)
//@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
//@AutoConfigureMockMvc
//@EnableAutoConfiguration
//@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
//@TestPropertySource(locations = ["classpath:application-test-container.properties"])
////@Execution(ExecutionMode.CONCURRENT)
//@TestInstance(TestInstance.Lifecycle.PER_CLASS)
//@Tag("test_container")
abstract class AbstractTestcontainersIntegrationTest {

    companion object {

        @Container
        private val ydb = System
            .setProperty("YDB_DOCKER_IMAGE", "cr.yandex/crptqonuodf51kdj7a7d/ydb-enterpise:25.1.4.ent.5")
            .also { System.setProperty("api.version", "1.44") }
            .let { YdbDockerContainer(YdbEnvironment(), PortsGenerator()) }
//            .apply {
////                withEnv("INIT_YDB_SCRIPT", "/restore.sh")
//                withEnv("YDB_FEATURE_FLAGS", "enable_views")
//                withCopyToContainer(
//                    MountableFile.forClasspathResource("/restore.sh"),
//                    "./restore.sh"
//                )
//                withCopyToContainer(
//                    MountableFile.forClasspathResource("/ydb_backup.tar"),
//                    "./ydb_backup.tar"
//                )
//            }
    }

    internal class Initializer : ApplicationContextInitializer<ConfigurableApplicationContext> {
        override fun initialize(applicationContext: ConfigurableApplicationContext) {
            ydb.init()
            ydb.start()
            ydb.execInContainer("sh", "/restore.sh")
            TestPropertyValues.of(
                "spring.datasource.url=" + jdbcUrl(),
            ).applyTo(applicationContext.environment)
        }

        fun jdbcUrl(): String {
            val jdbc = StringBuilder("jdbc:ydb:grpc://")
                .append(ydb.host)
                .append(":")
                .append(ydb.firstMappedPort)


            return jdbc.toString()
        }
    }

}