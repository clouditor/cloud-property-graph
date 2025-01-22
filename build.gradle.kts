plugins {
    // Apply the Kotlin JVM plugin to add support for Kotlin.
    id("org.jetbrains.kotlin.jvm") version "1.9.20" apply false
    kotlin("plugin.serialization") version "1.9.20" apply false
    id("com.diffplug.spotless") version "5.12.1"
}

subprojects {
    apply(plugin = "java")
    apply(plugin = "jacoco")
    apply(plugin = "org.jetbrains.kotlin.jvm")
    apply(plugin = "com.diffplug.spotless")

    repositories {
        mavenCentral()

        ivy {
            setUrl("https://download.eclipse.org/tools/cdt/releases/11.3/cdt-11.3.1/plugins")
            metadataSources {
                artifact()
            }
            patternLayout {
                artifact("/[organisation].[module]_[revision].[ext]")
            }
        }
    }
}