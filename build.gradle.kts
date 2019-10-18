import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.3.50"
    id("com.github.johnrengelman.shadow") version "5.1.0" apply true
    application
}

val botMainClass = "moe.kageru.kagebot.KagebotKt"
application {
    mainClassName = botMainClass
}

tasks.withType<Jar> {
    manifest {
        attributes(
            mapOf(
                "Main-Class" to botMainClass
            )
        )
    }
}

group = "moe.kageru.kagebot"
version = "0.1"

repositories {
    mavenCentral()
    jcenter()
}

val test by tasks.getting(Test::class) {
    useJUnitPlatform { }
}

dependencies {
    implementation("com.uchuhimo:konf-core:0.20.0")
    implementation("com.uchuhimo:konf-toml:0.20.0")
    implementation("com.moandjiezana.toml:toml4j:0.7.2")
    implementation(kotlin("stdlib-jdk8"))
    implementation("org.javacord:javacord:3.0.4")
    implementation("org.mapdb:mapdb:3.0.7")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.3.2")
    implementation("org.jetbrains.kotlin:kotlin-reflect:1.3.50")
    implementation("com.fasterxml.jackson.core:jackson-annotations:2.10.0.pr3")

    testImplementation("io.kotlintest:kotlintest-runner-junit5:3.4.2")
    testImplementation("io.mockk:mockk:1.9.3")
    // these two are needed to access javacord internals (such as reading from sent embeds during tests)
    testImplementation("org.javacord:javacord-core:3.0.4")
    testImplementation("com.fasterxml.jackson.core:jackson-databind:2.10.0.pr3")
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}
