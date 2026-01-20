plugins {
    id("org.jetbrains.kotlin.jvm") version "2.1.10"
    id("org.jetbrains.kotlin.plugin.serialization") version "2.1.10"
    id("org.gradle.maven-publish")
}

group = "org.kotlinbitcointools"
version = "0.1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("io.ktor:ktor-client-core:3.3.3")
    implementation("io.ktor:ktor-client-cio:3.3.3")
    implementation("io.ktor:ktor-client-content-negotiation:3.3.3")
    implementation("io.ktor:ktor-serialization-kotlinx-json:3.3.3")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.8.0")

    testImplementation("org.jetbrains.kotlin:kotlin-test:2.1.10")
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

publishing {
    publications {
        create<MavenPublication>("release") {
            from(components["java"])
        }
    }

    publications.named<MavenPublication>("release") {
        this.groupId = project.group.toString()
        this.artifactId = project.name
        this.version = project.version.toString()

        pom {
            name.set("regtest-toolbox")
            description.set("A set of tools for the regtest connoisseur.")
            url.set("https://github.com/kotlin-bitcoin-tools")
            licenses {
                license {
                    name.set("The Apache License, Version 2.0")
                    url.set("https://github.com/kotlin-bitcoin-tools/regtest-toolbox/blob/master/LICENSE.txt")
                }
            }
            developers {
                developer {
                    id.set("thunderbiscuit")
                    name.set("thunderbiscuit")
                    email.set("thunderbiscuit@protonmail.com")
                }
            }
            scm {
                connection.set("scm:git:https://github.com:kotlin-bitcoin-tools/regtest-toolbox.git")
                developerConnection.set("scm:git:git@github.com:kotlin-bitcoin-tools/regtest-toolbox.git")
                url.set("https://github.com/kotlin-bitcoin-tools/regtest-toolbox")
            }
        }
    }
}
