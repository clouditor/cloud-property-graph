package io.clouditor.graph

import org.jboss.forge.roaster.model.source.JavaClassSource
import org.semanticweb.owlapi.model.OWLOntologyCreationException
import java.io.File
import java.io.FileWriter
import java.io.IOException

object SemanticNodeGenerator {
    @Throws(OWLOntologyCreationException::class)
    @JvmStatic
    fun main(args: Array<String>) {
        var outputBase = "../cloudpg/generated/main/java"
        if (args.isNotEmpty()) {
            outputBase = args[0]
        }

        // IMPORTANT: Only use OWL/XML or RDF/XML files
        val filepath = "resources/urn_webprotege_ontology_e4316a28-d966-4499-bd93-6be721055117.owx"
        val owl3 = OWLCloudOntology(filepath)
        val jcs = owl3.javaClassSources
        writeClassesToFolder(jcs, outputBase)
    }

    // Write java class files to filesystem
    private fun writeClassesToFolder(jcs: List<JavaClassSource>, outputBase: String) {
        var filename: String
        for (jcsElem in jcs) {
            filename = outputBase + "/" + jcsElem.getPackage().replace(".", "/") + "/" + jcsElem.name + ".java"

            // write to file
            val f = File(filename)
            val directory = f.parentFile
            if (!directory.exists()) {
                if (!directory.mkdirs()) {
                    println("Could not create base directory for file $outputBase")
                }
            }
            try {
                val fileWriter = FileWriter(f)
                fileWriter.write(jcsElem.toString())
                fileWriter.close()
                println("File written to: $outputBase")
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }
}