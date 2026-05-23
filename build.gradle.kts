plugins {
    kotlin("jvm") version "1.9.22"
    id("org.jetbrains.compose") version "1.6.10"
    application
}

repositories {
    google()
    mavenCentral()
}

application {
    mainClass.set("MainKt")
}

dependencies {
    implementation(compose.desktop.currentOs)
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.0")
    implementation("org.xerial:sqlite-jdbc:3.45.2.0")
}