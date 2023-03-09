rootProject.name = "cs346"

include("application", "console", "shared")

dependencyResolutionManagement {
    versionCatalogs {
        create("libs") {
            // constants
            version("jdk", "17")
            version("javafx", "18.0.2")

            // https://plugins.gradle.org/
            plugin("kotlin-lang", "org.jetbrains.kotlin.jvm").version("1.8.10")
            plugin("jlink", "org.beryx.jlink").version("2.26.0")
            plugin("javafx", "org.openjfx.javafxplugin").version("0.0.13")
            plugin("javamodularity", "org.javamodularity.moduleplugin").version("1.8.12")

            // https://mvnrepository.com/
            library("kotlin-coroutines", "org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4")
            library("sqlite", "org.xerial:sqlite-jdbc:3.40.1.0")
            library("exposed-core", "org.jetbrains.exposed:exposed-core:0.40.1")
            library("exposed-dao", "org.jetbrains.exposed:exposed-dao:0.40.1")
            library("exposed-jdbc", "org.jetbrains.exposed:exposed-jdbc:0.40.1")
            library("exposed-time", "org.jetbrains.exposed:exposed-java-time:0.40.1")
            library("junit-jupiter", "org.junit.jupiter:junit-jupiter:5.9.2")
            library("log4j", "org.apache.logging.log4j:log4j-slf4j-impl:2.20.0")
        }
    }
}
