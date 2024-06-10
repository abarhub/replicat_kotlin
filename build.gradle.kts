plugins {
    kotlin("jvm") version "1.9.23"
    kotlin("plugin.serialization") version "2.0.0"
}

group = "org.testspringboot3"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))
    implementation("io.javalin:javalin:6.1.3")
    implementation("org.slf4j:slf4j-simple:2.0.10")
    implementation("org.apache.logging.log4j:log4j-api:2.7")
    implementation("org.apache.logging.log4j:log4j-core:2.7")
    implementation("org.apache.logging.log4j:log4j-slf4j-impl:2.7")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.3")
    implementation("io.github.microutils:kotlin-logging-jvm:2.0.11")
    implementation("ch.qos.logback:logback-classic:1.2.6")
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(21)
}