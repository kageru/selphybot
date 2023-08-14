import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

apply {
  plugin("kotlin-kapt")
}
plugins {
  kotlin("jvm") version "1.9.0"
  id("com.github.johnrengelman.shadow") version "8.1.1" apply true
  application
}

val botMainClass = "moe.kageru.kagebot.KagebotKt"
application {
  mainClass.set(botMainClass)
}

tasks.withType<Jar> {
  manifest {
    attributes(
      mapOf(
        "Main-Class" to botMainClass,
      ),
    )
  }
}

group = "moe.kageru.kagebot"
version = "0.1"

repositories {
  mavenCentral()
  jcenter()
  maven {
    url = uri("https://dl.bintray.com/arrow-kt/arrow-kt/")
  }
}

val test by tasks.getting(Test::class) {
  useJUnitPlatform { }
}

val arrowVersion = "0.11.0"
dependencies {
  implementation("com.uchuhimo:konf-core:0.23.0")
  implementation("com.uchuhimo:konf-toml:0.23.0")
  implementation("org.javacord:javacord:3.8.0")
  implementation("org.mapdb:mapdb:3.0.8")
  implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.3.9")
  implementation("org.jetbrains.kotlin:kotlin-reflect:1.9.0")
  implementation("com.fasterxml.jackson.core:jackson-annotations:2.11.3")

  implementation("io.arrow-kt:arrow-core:$arrowVersion")
  implementation("io.arrow-kt:arrow-syntax:$arrowVersion")

  testImplementation("io.kotlintest:kotlintest-runner-junit5:3.4.2")
  testImplementation("io.mockk:mockk:1.10.0")
  // these two are needed to access javacord internals (such as reading from sent embeds during tests)
  testImplementation("org.javacord:javacord-core:3.8.0")
  testImplementation("com.fasterxml.jackson.core:jackson-databind:2.11.3")
}

tasks.withType<KotlinCompile> {
  kotlinOptions.jvmTarget = "20"
}
