package io.clouditor.graph

import org.apache.commons.lang3.StringUtils
import org.jboss.forge.roaster.model.source.JavaClassSource
import org.semanticweb.owlapi.model.OWLOntologyCreationException
import java.io.File
import java.io.FileWriter
import java.io.IOException
import java.util.*
import java.util.stream.Collectors

object SemanticNodeGenerator {
    @Throws(OWLOntologyCreationException::class)
    @JvmStatic
    fun main(args: Array<String>) {

        // TODO During development, the following parameters are default values
        var outputBaseGo = "output/go/"
        var packageNameGo = "voc"
        var outputBaseJava = "../cloudpg/generated/main/java/io/clouditor/graph/"
        var packageNameJava = "io.clouditor.graph"
        val resourceNameFromOwlFile = "Resource"

        // IMPORTANT: Only OWL/XML and RDF/XML are supported
        var owlInputPath = "resources/urn_webprotege_ontology_e4316a28-d966-4499-bd93-6be721055117.owx"
        if (args.isEmpty()) {
            print(
                """
    Please use the following parameters:  
    
    1st parameter: Ontology Input File (Only OWL/XML and RDF/XML are supported)2st parameter: Java package name
    3nd parameter: Output path for generated Java files (optional, but the order must be respected)
    4th parameter: Go package name
    5th parameter: Output path for generated Go Files (optional, but the order must be respected)
    """.trimIndent()
            )
        }
        when (args.size) {
            5 -> {
                owlInputPath = args[0]
                packageNameJava = args[1]
                outputBaseJava = checkPath(args[2])
                packageNameGo = args[3]
                outputBaseGo = checkPath(args[4])
            }
            4 -> {
                owlInputPath = args[0]
                packageNameJava = args[1]
                outputBaseJava = checkPath(args[2])
                packageNameGo = args[3]
            }
            3 -> {
                owlInputPath = args[0]
                packageNameJava = args[1]
                outputBaseJava = checkPath(args[2])
            }
            2 -> {
                owlInputPath = args[0]
                packageNameJava = args[1]
            }
            1 -> {
                owlInputPath = args[0]
            }
        }

        // Clear contents of previous output folders to guarantee full rebuild
        java.io.File(outputBaseJava).deleteRecursively()
        java.io.File(outputBaseGo).deleteRecursively()

        val owl3 = OWLCloudOntology(owlInputPath, resourceNameFromOwlFile)

        // Create java class sources
        val jcs = owl3.getJavaClassSources(packageNameJava)
        writeClassesToFolder(jcs, outputBaseJava)

        // Create Go sources
        val ontologyDescription = owl3.getGoStructs(packageNameGo)
        writeGoStringsToFolder(ontologyDescription, outputBaseGo, owl3)
    }

    private fun checkPath(outputBase: String): String {
        var tmpOutputBase = outputBase
        return if (tmpOutputBase[tmpOutputBase.length - 1] != '/') "/".let { tmpOutputBase += it; tmpOutputBase } else tmpOutputBase
    }

    // Create Go source code
    private fun createGoSourceCodeString(goSource: GoStruct, owl3: OWLCloudOntology): String {
        var goSourceCode = ""

        // Add auto-generated code text
        goSourceCode += autoGeneratedCodeText() + "\n"

        // Add copyright
        goSourceCode += clouditorCopyright() + "\n"

        // Add package name
        goSourceCode += """
             package ${goSource.packageName}
             
             
             """.trimIndent()

        // Add imports
        for (elem in goSource.dataProperties){
            if (getGoType(elem.propertyType) == "time.Duration" || getGoType(elem.propertyType) == "time.Time" ) {
                goSourceCode += "import \"time\"\n\n"
                break
            }
        }

        // Add type array, e.g., var BlockStorageType = []string{"BlockStorage", "Storage", "Resource"}
        if (goSource.resourceTypes.isNotEmpty())
            goSourceCode += addResourceTypeArray(goSource) + "\n\n"


        // Add struct description
        if (goSource.structDescription != "")
            goSourceCode += "// " + goSource.name + " is an entity in our Cloud ontology. " + goSource.structDescription + "\n"

        // Add struct
        goSourceCode += "type " + goSource.name + " struct {\n"

        // Check if parentClass exists
        val parentClassName = getParentClassName(goSource.parentClass)
        if (parentClassName != "") {
            goSourceCode += "\t*" + getParentClassName(goSource.parentClass)
        }

        // Add object properties
        goSourceCode += getObjectPropertiesForGoSource(goSource.objectProperties)

        // Add data properties
        goSourceCode += getDataPropertiesForGoSource(goSource.dataProperties)
        goSourceCode += "\n}\n\n"

        // Add method for interface
        // TODO(anatheka): Comparison with Authenticity, Authorization and Confidentiality is hacky, we have to put that as annotation in the ontology.
        if (owl3.interfaceList.contains(goSource.parentClass) || goSource.parentClass == "Authenticity" || goSource.parentClass == "Authorization" || goSource.parentClass == "Confidentiality") {
                goSourceCode += getInterfaceMethod(goSource)
        }

        return goSourceCode
    }

    // Return the golang interface method for the given struct
    private fun getInterfaceMethod(gs: GoStruct): String {
        val receiverType = gs.name

        return "func (*$receiverType) Type() string {\n\treturn \"$receiverType\"\n}"

    }

    // Change property type to GO type
    private fun getGoType(type: String): String {
        val goType: String = when (type) {
            "String" -> "string"
            "float" -> "float32"
            "boolean" -> "bool"
            "java.time.Duration" -> "time.Duration"
            "java.time.ZonedDateTime" -> "time.Time"
            "java.util.Map<String, String>" -> "map[string]string"
            "java.util.ArrayList<Short>" -> "[]uint16"
            "java.util.ArrayList<String>" -> "[]string"
            "Short" -> "uint16"
            "de.fraunhofer.aisec.cpg.graph.declarations.FunctionDeclaration" ->                 // TODO What do we need here?
                "string"
            "de.fraunhofer.aisec.cpg.graph.statements.expressions.Expression" ->                 // TODO What do we need here?
                "string"
            "de.fraunhofer.aisec.cpg.graph.Node" -> "string"
            "de.fraunhofer.aisec.cpg.graph.statements.expressions.CallExpression" -> "string"
            "java.util.List<de.fraunhofer.aisec.cpg.graph.declarations.TranslationUnitDeclaration>" -> "[]string"
            "java.util.List<de.fraunhofer.aisec.cpg.graph.statements.expressions.CallExpression>" -> "[]string"
            else -> type
        }
        return goType
    }

    private fun getParentClassName(parentClass: String): String {
        if (parentClass == "" || parentClass == "owl:Thing") return ""
        val parentClassSplit = parentClass.split("\\.").toTypedArray()
        return parentClassSplit[parentClassSplit.size - 1]
    }

    private fun getObjectPropertiesForGoSource(properties: List<Properties>): String {

        var propertiesStringSource = ""
        for (property in properties) {
            propertiesStringSource +=
                if (!property.isRootClassNameResource && !property.isInterface && property.propertyProperty == "hasMultiple" ){ // must be a slice
                    """
	${StringUtils.capitalize(property.propertyName)}	""" + getAdjustedPropertyType("[]" + property.propertyType) + " \t`json:\"" + property.propertyName + "\"`"
        }else if (!property.isRootClassNameResource && !property.isInterface) { // nothing special
                    """
	${StringUtils.capitalize(property.propertyName)}	"""  + getAdjustedPropertyType(property.propertyType) + " \t`json:\"" + property.propertyName + "\"`"
                } else if (!property.isRootClassNameResource && property.isInterface) { // is an interface
                    """
	${StringUtils.capitalize(property.propertyName)}	""" + StringUtils.capitalize(property.propertyType) + " \t`json:\"" + property.propertyName + "\"`"
                } else if (property.propertyProperty == "hasMultiple" || property.propertyProperty == "offersMultiple") { // must be a []ResourceID
                    """
	${StringUtils.capitalize(property.propertyName)}	[]ResourceID""" + "\t" + """`json:"${property.propertyName}"`"""
        } else if (property.propertyProperty == "has") { // must be ResourceID
                    """
	${StringUtils.capitalize(property.propertyName)}	ResourceID""" + "\t" + """`json:"${property.propertyName}"`"""
                } else { // TODO should that be always a []ResourceID?
                """
	${StringUtils.capitalize(property.propertyName)}	[]ResourceID""" + "\t" + """`json:"${property.propertyName}"`"""
            }
        }
        return propertiesStringSource
    }

    private fun getDataPropertiesForGoSource(properties: List<Properties>): String {
        var propertiesStringSource = ""
        for (property in properties) {
            if (property.propertyDescription != "")
                propertiesStringSource += "\n\t// ${property.propertyDescription}"
            property.propertyType.let {
                propertiesStringSource += """
            ${StringUtils.capitalize(property.propertyName)}	${getGoType(it)}""" + "\t" + """`json:"${property.propertyName}"`"""
            }
        }
        return propertiesStringSource
    }

    // Returns the resource type array
    private fun addResourceTypeArray(struct: GoStruct): String {
        var resourceTypeString = "var " + struct.name + "Type = []string {"

        for ((counter, type) in struct.resourceTypes.withIndex()) {
            resourceTypeString += if (counter == struct.resourceTypes.size-1){
                "\"$type\"}"
            } else {
                "\"$type\", "
            }
        }

        return resourceTypeString
    }


    // Checks property type and if it is a security feature (regarding the ontology), return the adjusted type
    private fun getAdjustedPropertyType(type: String): String {
        val propType: String = when (type) {
            "Authenticity" -> "IsAuthenticity"
            "[]Authenticity" -> "[]IsAuthenticity"
            "Authorization" -> "IsAuthorization"
            "[]Authorization" -> "[]IsAuthorization"
            "AtRestEncryption" -> "IsAtRestEncryption"
            else -> addPointer(type)
        }


        return propType
    }

    // Adds a pointer, e.g., []Backup -> []*Backup or Backup -> *Backup
    private fun addPointer(elem: String): String {
        if (elem.startsWith("[]")) {
            var res = elem.drop(2)
            return "[]*$res"
        } else {
            return "*$elem"
        }
    }

    // Write Go source code to filesystem
    private fun writeGoStringsToFolder(goSources: List<GoStruct>, outputBase: String, owl3: OWLCloudOntology) {
        var filepath: String
        for (goSource in goSources) {
            filepath = getGoFilepath(goSource.name, outputBase)
            val f = File(filepath)
            val directory = f.parentFile
            if (!directory.exists()) {
                if (!directory.mkdirs()) {
                    println("Could not create base directory for file $outputBase")
                }
            }
            try {
                val fileWriter = FileWriter(f)
                fileWriter.write(createGoSourceCodeString(goSource, owl3))
                fileWriter.close()
                println("File written to: $filepath")
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    private fun getGoFilepath(name: String, outputBase: String): String {
        val filepath: String
        val filenameList: List<String?>? =
            Arrays.stream(name.split("(?<!(^|[A-Z]))(?=[A-Z])|(?<!^)(?=[A-Z][a-z])".toRegex()).toTypedArray())
                .map { obj: String -> obj.lowercase(Locale.getDefault()) }
                .collect(Collectors.toList())
        val filename: String = java.lang.String.join("_", filenameList)

        // Create filepath
        filepath = "$outputBase$filename.go"
        return filepath
    }

    // Write java class files to filesystem
    private fun writeClassesToFolder(jcs: List<JavaClassSource>, outputBase: String) {
        var filename: String
        for (jcsElem in jcs) {
            filename = outputBase + jcsElem.name + ".java"

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
                var outputText = ""
                outputText += autoGeneratedCodeText() + "\n\n"
                outputText += jcsElem.toString()
                fileWriter.write(outputText)
                fileWriter.close()
                println("File written to: $filename")
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    // Returns text with auto-generation message
    private fun autoGeneratedCodeText(): String{
        return "// Auto-generated code by owl2java (https://github.com/clouditor/cloud-property-graph)"
    }

    private fun clouditorCopyright():String {
       return """
// Copyright 2022 Fraunhofer AISEC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
//
//           ${"$"}$\                           ${"$"}$\ ${"$"}$\   ${"$"}$\
//           ${"$"}$ |                          ${"$"}$ |\__|  ${"$"}$ |
//  ${"$"}${"$"}${"$"}${"$"}${"$"}${"$"}$\ ${"$"}$ | ${"$"}${"$"}${"$"}${"$"}${"$"}$\  ${"$"}$\   ${"$"}$\  ${"$"}${"$"}${"$"}${"$"}${"$"}${"$"}$ |${"$"}$\ ${"$"}${"$"}${"$"}${"$"}${"$"}$\    ${"$"}${"$"}${"$"}${"$"}${"$"}$\   ${"$"}${"$"}${"$"}${"$"}${"$"}$\
// ${"$"}$  _____|${"$"}$ |${"$"}$  __${"$"}$\ ${"$"}$ |  ${"$"}$ |${"$"}$  __${"$"}$ |${"$"}$ |\_${"$"}$  _|  ${"$"}$  __${"$"}$\ ${"$"}$  __${"$"}$\
// ${"$"}$ /      ${"$"}$ |${"$"}$ /  ${"$"}$ |${"$"}$ |  ${"$"}$ |${"$"}$ /  ${"$"}$ |${"$"}$ |  ${"$"}$ |    ${"$"}$ /  ${"$"}$ |${"$"}$ | \__|
// ${"$"}$ |      ${"$"}$ |${"$"}$ |  ${"$"}$ |${"$"}$ |  ${"$"}$ |${"$"}$ |  ${"$"}$ |${"$"}$ |  ${"$"}$ |${"$"}$\ ${"$"}$ |  ${"$"}$ |${"$"}$ |
// \${"$"}${"$"}${"$"}${"$"}${"$"}$\  ${"$"}$ |\${"$"}${"$"}${"$"}${"$"}$   |\${"$"}${"$"}${"$"}${"$"}$   |\${"$"}${"$"}${"$"}${"$"}${"$"}$  |${"$"}$ |  \${"$"}${"$"}$   |\${"$"}${"$"}${"$"}${"$"}$   |${"$"}$ |
//  \_______|\__| \______/  \______/  \_______|\__|   \____/  \______/ \__|
//
// This file is part of Clouditor Community Edition.
"""
    }
}