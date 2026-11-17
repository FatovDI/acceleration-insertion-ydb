import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
	id("org.springframework.boot") version "3.1.2"
	id("io.spring.dependency-management") version "1.1.0"
	id("org.jetbrains.kotlin.kapt") version "1.8.10"
	id("me.champeau.jmh") version "0.6.8"
	kotlin("jvm") version "1.8.21"
	kotlin("plugin.spring") version "1.8.21"
}

group = "com.example"
java.sourceCompatibility = JavaVersion.VERSION_17

repositories {
	mavenCentral()
}

dependencies {
	implementation("org.springframework.boot:spring-boot-starter")
	implementation("org.springframework.boot:spring-boot-starter-web")
	implementation("org.springframework.boot:spring-boot-starter-data-jpa")
	implementation("org.jetbrains.kotlin:kotlin-reflect")
	implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.1")
	implementation("jakarta.persistence:jakarta.persistence-api")
    implementation("org.liquibase:liquibase-core:4.29.2")
	implementation("org.openjdk.jmh:jmh-core:1.36")
    implementation("tech.ydb.jdbc:ydb-jdbc-driver:2.3.20")
    implementation("tech.ydb.dialects:hibernate-ydb-dialect:1.4.1")
    implementation("tech.ydb.dialects:liquibase-ydb-dialect:1.1.1")

	kaptJmh("org.openjdk.jmh:jmh-generator-annprocess:1.36")

	testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.testcontainers:testcontainers:1.20.6")
    testImplementation("tech.ydb.test:ydb-junit5-support:2.3.14")
}

allOpen {
	annotation("org.openjdk.jmh.annotations.State")
	annotation("javax.persistence.Entity")
	annotation("javax.persistence.MappedSuperclass")
	annotation("javax.persistence.Embeddable")
}

tasks.withType<KotlinCompile> {
	kotlinOptions {
		freeCompilerArgs = listOf("-Xjsr305=strict")
		jvmTarget = "17"
	}
}

tasks.withType<Test> {
	useJUnitPlatform{
		excludeTags("stressTest")
	}
}
