import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("org.jetbrains.kotlin.jvm") version "1.8.20-RC"
    kotlin("plugin.serialization") version "1.8.20-RC"
    id("org.jetbrains.kotlin.kapt") version "1.8.20-RC"
    id("org.jetbrains.kotlin.plugin.allopen") version "1.8.20-RC"
    id("com.github.johnrengelman.shadow") version "7.1.2"
    id("io.micronaut.application") version "3.6.3"
}

version = "2.0.0"
group = "kiinse.me.zonezero.api"
java.sourceCompatibility = JavaVersion.VERSION_19

repositories {
    mavenCentral()
}

dependencies {
    kapt("io.micronaut:micronaut-http-validation")
    runtimeOnly("ch.qos.logback:logback-classic")

    implementation("io.micronaut:micronaut-runtime")
    implementation("io.micronaut.kotlin:micronaut-kotlin-runtime")
    implementation("io.micronaut:micronaut-http-client")
    implementation("io.micronaut:micronaut-jackson-databind")
    implementation("jakarta.annotation:jakarta.annotation-api")
    implementation("io.micronaut:micronaut-validation")
    runtimeOnly("ch.qos.logback:logback-classic")

    implementation("org.mongodb:mongo-java-driver:3.12.12")
    implementation("com.auth0:java-jwt:4.3.0")
    implementation("org.slf4j:slf4j-api:2.0.5")
    implementation("org.slf4j:slf4j-log4j12:2.0.5")
    implementation("org.apache.logging.log4j:log4j-core:2.20.0")
    implementation("org.apache.logging.log4j:log4j-api:2.20.0")
    runtimeOnly("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("org.tomlj:tomlj:1.1.0")
    implementation("com.sun.mail:jakarta.mail:2.0.1")
    implementation("io.sentry:sentry:6.16.0")
    implementation("com.vdurmont:semver4j:3.1.0")
    implementation("es.atrujillo.mjml:mjml-rest-client:2.0.1")

    implementation(project(":ZoneZeroApi-Core"))

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.0-Beta")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.5.0")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
}

application {
    mainClass.set("kiinse.me.zonezero.api.ZoneZeroAPIKt")
}

kapt {
    useBuildCache = false
}

graalvmNative.toolchainDetection.set(false)
micronaut {
    runtime("jetty")
    processing {
        incremental(true)
        annotations("kiinse.me.zonezero.api.*")
    }
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


val compileKotlin: KotlinCompile by tasks
compileKotlin.kotlinOptions {
    jvmTarget = "19"
}
val compileTestKotlin: KotlinCompile by tasks
compileTestKotlin.kotlinOptions {
    jvmTarget = "19"
}