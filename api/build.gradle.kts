plugins {
    kotlin("multiplatform")
    alias(libs.plugins.serialization)
}

group = "com.github.huymaster"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
}

kotlin {
    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(libs.serialization.core)
                implementation(libs.serialization.json)
                implementation(libs.coroutines.core)
            }
        }

        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
            }
        }
    }

    jvm()
    js(IR) {
        browser()
        binaries.executable()
    }
}