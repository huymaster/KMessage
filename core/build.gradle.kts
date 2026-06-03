plugins {
    kotlin("jvm")
    application
    idea
    alias(libs.plugins.ktor)
    alias(libs.plugins.serialization)
}

repositories {
    mavenCentral()
}

idea {
    module {
        isDownloadJavadoc = true
        isDownloadSources = true
    }
}

application {
    mainClass.set("com.github.huymaster.server.core.MainKt")
}

dependencies {
    implementation(project(":api"))
    implementation(platform(libs.ktor.bom))

    implementation(libs.serialization.core)
    implementation(libs.serialization.json)
    implementation(libs.serialization.cbor)

    implementation(libs.coroutines.core)
    implementation(libs.coroutines.reactive)

    implementation(libs.postgresql)

    implementation(libs.bouncy.castle)

    implementation(libs.netty)
    implementation(libs.netty.epoll)

    implementation(libs.hikari)

    implementation(libs.argon2)

    implementation(libs.lettuce)

    implementation(libs.minio)

    implementation(libs.ktor.server.core)

    implementation(libs.ktor.server.websockets)
    implementation(libs.ktor.server.html.builder)
    implementation(libs.ktor.server.host.common)
    implementation(libs.ktor.server.status.pages)
    implementation(libs.ktor.server.call.logging)
    implementation(libs.ktor.server.auto.head.response)
    implementation(libs.ktor.server.resources)
    implementation(libs.ktor.serialization.kotlinx.json)
    implementation(libs.ktor.serialization.kotlinx.cbor)
    implementation(libs.ktor.server.content.negotiation)
    implementation(libs.ktor.server.auth)
    implementation(libs.ktor.server.auth.jwt)
    implementation(libs.ktor.server.cors)
    implementation(libs.ktor.server.caching.headers)
    implementation(libs.ktor.server.compression)
    implementation(libs.ktor.server.compression.zstd)
    implementation(libs.ktor.server.conditional.headers)
    implementation(libs.ktor.server.http.redirect)
    implementation(libs.ktor.server.hsts)
    implementation(libs.ktor.server.forwarded.header)
    implementation(libs.ktor.server.netty)

    implementation(libs.kotlinx.html)
    implementation(libs.kotlin.css)

    implementation(platform(libs.koin.bom))
    implementation(libs.koin.core)
    implementation(libs.koin.ktor)
    implementation(libs.koin.logger.slf4j)

    implementation(libs.ktorm.core)
    implementation(libs.ktorm.psql)

    implementation(libs.snake.yaml)
    implementation(libs.logback.classic)

    testImplementation(kotlin("test"))
    testImplementation("io.ktor:ktor-client-core")
    testImplementation("io.ktor:ktor-client-cio")
    testImplementation("io.ktor:ktor-client-content-negotiation")
    testImplementation("io.ktor:ktor-client-serialization")
}

kotlin {
    jvmToolchain(25)
}

tasks.test {
    useJUnitPlatform()
}

tasks.jar {
    manifest {
        attributes["Main-Class"] = "com.github.huymaster.server.core.MainKt"
    }
    exclude("META-INF/*.RSA", "META-INF/*.SF", "META-INF/*.DSA")
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    from(configurations.runtimeClasspath.get().filter { it.exists() }.map { if (it.isDirectory) it else zipTree(it) })
}