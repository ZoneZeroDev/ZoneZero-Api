plugins {
    id("org.jetbrains.kotlin.jvm") version "1.8.20-RC"
    kotlin("plugin.serialization") version "1.8.20-RC"
    id("org.jetbrains.kotlin.kapt") version "1.8.20-RC"
    id("org.jetbrains.kotlin.plugin.allopen") version "1.8.20-RC"
    id("com.github.johnrengelman.shadow") version "7.1.2"
}

group = "kiinse.me.zonezero.api"
description = "ZoneZero-Api-Core"
version = "2.0.0"
java.sourceCompatibility = JavaVersion.VERSION_19

repositories {
    mavenCentral()
}

dependencies {
    implementation("io.micronaut:micronaut-http:3.8.0")
    implementation("commons-codec:commons-codec:1.15")
    implementation("commons-io:commons-io:2.11.0")
    implementation("org.apache.commons:commons-lang3:3.12.0")
    implementation("org.jetbrains:annotations:24.0.1")
    implementation("org.slf4j:slf4j-api:2.0.5")
    implementation("org.slf4j:slf4j-log4j12:2.0.5")
    implementation("org.apache.httpcomponents:fluent-hc:4.5.14")
    implementation("org.mindrot:jbcrypt:0.4")
    implementation("com.auth0:java-jwt:4.3.0")

    implementation("org.tomlj:tomlj:1.1.0")

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.0-Beta")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.5.0")
    implementation("org.mongodb:mongo-java-driver:3.12.12")
    implementation("org.apache.logging.log4j:log4j-core:2.20.0")
    implementation("org.apache.logging.log4j:log4j-api:2.20.0")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
}

tasks {
    compileKotlin {
        kotlinOptions {
            jvmTarget = "19"
        }
    }
    compileTestKotlin {
        kotlinOptions {
            jvmTarget = "19"
        }
    }
}
