import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.3.31"
    application
}

application {
    mainClassName = "moe.kageru.kagebot.KagebotKt"
}

group = "moe.kageru.kagebot"
version = "0.1"

repositories {
    mavenCentral()
}

val test by tasks.getting(Test::class) {
    useJUnitPlatform { }
}

dependencies {
    implementation("com.moandjiezana.toml:toml4j:0.7.2")
    implementation(kotlin("stdlib-jdk8"))
    implementation("org.javacord:javacord:3.0.4")

    testImplementation("io.kotlintest:kotlintest-runner-junit5:3.3.2")
    testImplementation("io.mockk:mockk:1.9.3")
    // these two are needed to access javacord internals (such as reading from sent embeds during tests)
    testImplementation("org.javacord:javacord-core:3.0.4")
    testImplementation("com.fasterxml.jackson.core:jackson-databind:2.9.9")
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}