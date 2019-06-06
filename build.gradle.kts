import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.3.31"
}

group = "moe.kageru.kagebot"
version = "0.1"

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib-jdk11"))
    compile("com.electronwill.night-config:toml:3.6.0")
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.11"
}