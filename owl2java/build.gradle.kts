plugins {
    id("org.jetbrains.kotlin.jvm") version "1.6.0"
    application
}

repositories {
    mavenCentral()
}

dependencies {
    // owl-api
    implementation("net.sourceforge.owlapi:owlapi-distribution:4.5.4")
    implementation("net.sourceforge.owlapi:org.semanticweb.hermit:1.3.8.413")

    // roaster
    compile("org.jboss.forge.roaster:roaster-api:2.22.2.Final")
    compile("org.jboss.forge.roaster:roaster-jdt:2.22.2.Final")
    compile("org.apache.jena:jena-arq:3.4.0")
}

application {
    mainClassName = "io.clouditor.graph.SemanticNodeGenerator"
}