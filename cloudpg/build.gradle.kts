import org.jetbrains.kotlin.com.intellij.openapi.vfs.StandardFileSystems.jar

/*
 * This file was generated by the Gradle 'init' task.
 *
 * This generated file contains a sample Kotlin application project to get you started.
 */

plugins {
    application
    idea
    `maven-publish`
    id("org.jetbrains.kotlinx.benchmark") version "0.4.4"
    // use this plugin to set all classes open which is required for kotlinx benchmark: id("org.jetbrains.kotlin.plugin.allopen") version "1.7.20-Beta"
}

// when using the allOpen plugin, use 'allOpen{annotation("org.openjdk.jmh.annotations.State")}' to configure the annotations

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
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
    kotlinOptions {
        jvmTarget = "17"
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

    maven {
        setUrl("https://oss.sonatype.org/content/groups/public/")
    }

    ivy {
        setUrl("https://download.eclipse.org/tools/cdt/releases/11.0/cdt-11.0.0/plugins")
        metadataSources {
            artifact()
        }
        patternLayout {
            artifact("/[organisation].[module]_[revision].[ext]")
        }
    }
}

dependencies {
    implementation("org.junit.jupiter:junit-jupiter:5.7.0")
    val version = "7.0.0"

    implementation("de.fraunhofer.aisec:cpg-core:$version")
    implementation("de.fraunhofer.aisec:cpg-analysis:$version")
    implementation("de.fraunhofer.aisec:cpg-language-go:$version")
    implementation("de.fraunhofer.aisec:cpg-language-python:$version")
    implementation("de.fraunhofer.aisec:cpg-language-typescript:$version")
    implementation("de.fraunhofer.aisec:cpg-language-java:$version")
    implementation("de.fraunhofer.aisec:cpg-language-cxx:$version")

    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.13.+")
    implementation ("org.xmlunit:xmlunit-core:2.9.0")
    implementation("org.xmlunit:xmlunit-matchers:2.9.0")

    api("org.neo4j", "neo4j-ogm-core", "4.0.5")
    api("org.neo4j", "neo4j-ogm", "4.0.5")
    api("org.neo4j", "neo4j-ogm-bolt-driver", "4.0.5")

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

tasks.withType<Test>().configureEach {
    // Some tests in our testing library (intentionally) fail, so we need to ignore those tests
    ignoreFailures = true
    // increase heap for tests/benchmarks of large code files
    // jvmArgs = mutableListOf("-Xms1024m", "-Xmx1024m")
}

// configuration for kotlinx benchmark
benchmark {
    configurations {
        named("main"){
            reportFormat = "text"
            mode = "avgt"
        }
    }
    targets {
        register("test")
    }
}

kotlin {
    sourceSets {
        test {
            dependencies {
                implementation("org.jetbrains.kotlinx:kotlinx-benchmark-runtime:0.4.4")
            }
        }
    }
}

// required for kotlinx benchmark
tasks.withType<Zip> {
    isZip64 = true
}
