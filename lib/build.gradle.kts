plugins {
    id("org.jetbrains.kotlin.jvm") version "2.3.0"
    id("org.jetbrains.kotlin.plugin.serialization") version "2.3.0"
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("io.ktor:ktor-client-core:3.3.3")
    implementation("io.ktor:ktor-client-cio:3.3.3")
    implementation("io.ktor:ktor-client-content-negotiation:3.3.3")
    implementation("io.ktor:ktor-serialization-kotlinx-json:3.3.3")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.8.0")
}

testing {
    suites {
        // Configure the built-in test suite
        val test by getting(JvmTestSuite::class) {
            // Use Kotlin Test test framework
            useKotlinTest("1.9.20")
        }
    }
}

tasks.test {
    testLogging {
        showStandardStreams = true
    }
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}
