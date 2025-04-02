plugins {
    kotlin("jvm") version "2.1.20"
    kotlin("plugin.serialization") version "2.1.20"
}

group = "com.rusine"
version = "1.0"

repositories {
    mavenCentral()
}

dependencies {
    implementation("dev.schlaubi.lavakord:kord:9.1.0")
    implementation("dev.kord:kord-core:0.15.0")
    implementation("org.slf4j:slf4j-simple:2.0.7")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.8.1")
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}