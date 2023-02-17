import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    application
    kotlin("jvm") version "1.6.20"
    id("org.beryx.jlink") version "2.25.0"
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
    implementation(project(":application"))
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}

tasks.withType<Jar> {
    manifest {
        attributes["Main-Class"] = "cs346.console.MainKt"
    }
}

application {
    mainModule.set("console")
    mainClass.set("cs346.console.MainKt")
}

val run by tasks.getting(JavaExec::class) {
    standardInput = System.`in`
}
