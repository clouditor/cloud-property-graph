/*
 * This file was generated by the Gradle 'init' task.
 *
 * This generated file contains a sample Kotlin application project to get you started.
 */

plugins {
    application
    idea
    `maven-publish`
}

group = "io.clouditor"

val generatedDir = "${projectDir}/generated"

configure<org.gradle.plugins.ide.idea.model.IdeaModel> {
    module {
        // mark as generated sources for IDEA
        generatedSourceDirs.add(File("${generatedDir}/main/java"))
    }
}

java {
    sourceSets["main"].java {
        srcDir("${generatedDir}/main/java")
    }
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
    kotlinOptions {
        jvmTarget = "11"
        useIR = true
    }
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["java"])
        }
    }
}

repositories {
    mavenCentral()

    maven { setUrl("https://jitpack.io") }

    ivy {
        setUrl("https://download.eclipse.org/tools/cdt/releases/10.2/cdt-10.2.0/plugins")
        metadataSources {
            artifact()
        }
        patternLayout {
            artifact("/[organisation].[module]_[revision].[ext]")
        }
    }
}

dependencies {
    //implementation("de.fraunhofer.aisec", "cpg", "0.0.0-SNAPSHOT") {
    implementation("com.github.Fraunhofer-AISEC:cpg:4f62631b") {
        isChanging = true
    }

    api("org.neo4j", "neo4j-ogm-core", "3.2.21")
    api("org.neo4j", "neo4j-ogm", "3.2.21")
    api("org.neo4j", "neo4j-ogm-bolt-driver", "3.2.21")

    implementation(platform("org.jetbrains.kotlin:kotlin-bom"))

    implementation("org.jruby:jruby-core:9.2.17.0")

    implementation("info.picocli:picocli:4.6.1")
    //annotationProcessor("info.picocli:picocli-codegen:4.6.1")

    implementation("com.azure.resourcemanager:azure-resourcemanager:2.3.0")
    implementation("com.azure.resourcemanager:azure-resourcemanager-loganalytics:1.0.0-beta.2")
    implementation("com.azure:azure-identity:1.2.0")

    implementation("io.kubernetes:client-java:10.0.0")
    implementation("com.microsoft.azure:adal4j:1.6.6")

    implementation("com.fasterxml.jackson.core:jackson-databind:2.11.2")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.11.2")
    implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-yaml:2.11.2")

    implementation("com.charleskorn.kaml:kaml:0.23.0")
    
    // Use the Kotlin JDK 8 standard library.
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")

    // Use the Kotlin test library.
    testImplementation("org.jetbrains.kotlin:kotlin-test")

    // Use the Kotlin JUnit integration.
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit")
}

application {
    // Define the main class for the application.
    mainClassName = "io.clouditor.graph.AppKt"
}

tasks.named("compileJava") {
    dependsOn(":spotlessApply")
}

spotless {
    kotlin {
        ktfmt().kotlinlangStyle()
    }
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    dependsOn("spotlessApply")
}
