import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

// https://github.com/gradle/gradle/issues/22797
@Suppress("DSL_SCOPE_VIOLATION")
plugins {
    application
    alias(libs.plugins.kotlin.lang)
    alias(libs.plugins.javamodularity)
    alias(libs.plugins.javafx)
    alias(libs.plugins.jlink)
    id("org.jetbrains.kotlin.plugin.serialization") version "1.6.10"
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
    implementation(project(":shared"))
    implementation(libs.kotlin.coroutines)
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.3.2")
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

application {
    mainModule.set("application")
    mainClass.set("cs346.application.Main")
}

javafx {
    // version is determined by the plugin above
    version = libs.versions.javafx.get()
    modules = listOf("javafx.controls", "javafx.graphics")
}

// https://stackoverflow.com/questions/74453018/jlink-package-kotlin-in-both-merged-module-and-kotlin-stdlib
jlink {
    forceMerge("kotlin")
    launcher {
        name= "notes"
    }
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(libs.versions.jdk.get()))
    }
}