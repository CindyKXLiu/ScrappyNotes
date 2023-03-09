import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

// https://github.com/gradle/gradle/issues/22797
@Suppress("DSL_SCOPE_VIOLATION")
plugins {
    application
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
    implementation(project(":shared"))
    testImplementation(libs.junit.jupiter)
}

tasks.test {
    useJUnitPlatform()
}

application {
    mainModule.set("console")
    mainClass.set("cs346.console.MainKt")
}

val run by tasks.getting(JavaExec::class) {
    standardInput = System.`in`
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}
