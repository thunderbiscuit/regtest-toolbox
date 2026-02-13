import org.jlleitschuh.gradle.ktlint.reporter.ReporterType

plugins {
    id("org.jetbrains.kotlin.jvm") version "2.3.0"
    id("org.jetbrains.kotlin.plugin.serialization") version "2.3.0"
    id("org.jlleitschuh.gradle.ktlint") version "14.0.1"
    id("com.vanniktech.maven.publish") version "0.36.0"
}

group = "org.kotlinbitcointools"
version = "0.1.0"

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
        languageVersion = JavaLanguageVersion.of(11)
    }
}

mavenPublishing {
    coordinates(
        groupId = group.toString(),
        artifactId = "regtest-toolbox",
        version = version.toString()
    )

    pom {
        name.set("regtest-toolbox")
        description.set("A set of tools for the regtest connoisseur.")
        inceptionYear.set("2026")
        url.set("https://github.com/thunderbiscuit/regtest-toolbox")
        licenses {
            license {
                name.set("The Apache License, Version 2.0")
                url.set("https://www.apache.org/licenses/LICENSE-2.0.txt")
                distribution.set("repo")
            }
        }
        developers {
            developer {
                id.set("thunderbiscuit")
                name.set("thunderbiscuit")
                url.set("https://github.com/thunderbiscuit")
            }
        }
        scm {
            url.set("https://github.com/thunderbiscuit/regtest-toolbox")
            connection.set("scm:git:git://github.com/thunderbiscuit/regtest-toolbox.git")
            developerConnection.set("scm:git:ssh://git@github.com/thunderbiscuit/regtest-toolbox.git")
        }
    }
}

mavenPublishing {
    publishToMavenCentral()
    signAllPublications()
}

ktlint {
    version = "1.8.0"
    ignoreFailures = false
    reporters {
        reporter(ReporterType.PLAIN).apply { outputToConsole = true }
    }
}
