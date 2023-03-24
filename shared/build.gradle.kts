import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

@Suppress("DSL_SCOPE_VIOLATION")
plugins {
    `java-library`
    alias(libs.plugins.kotlin.lang)
    alias(libs.plugins.javamodularity)
}

group = "cs346"
version = "1.0.0"

val compileKotlin: KotlinCompile by tasks
val compileJava: JavaCompile by tasks
compileJava.destinationDirectory.set(compileKotlin.destinationDirectory)

repositories {
    mavenCentral()
}

dependencies {
    implementation(libs.sqlite)
    implementation(libs.exposed.core)
    implementation(libs.exposed.jdbc)
    implementation(libs.exposed.time)
    implementation(libs.sl4j.api)
    implementation(libs.sl4j.simple)
    implementation("com.google.code.gson:gson:2.10.1")
    testImplementation(libs.junit.jupiter)
}

tasks.test {
    useJUnitPlatform()
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(libs.versions.jdk.get()))
    }
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(libs.versions.jdk.get()))
    }
}