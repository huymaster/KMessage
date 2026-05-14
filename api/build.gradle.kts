plugins {
    kotlin("jvm")
    alias(libs.plugins.serialization)
}

group = "com.github.huymaster"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation(libs.serialization.core)
    implementation(libs.serialization.json)
    implementation(libs.serialization.cbor)
    implementation(libs.coroutines.core)

    implementation(libs.bouncy.castle)

    testImplementation(kotlin("test"))
}