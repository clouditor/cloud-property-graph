package io.clouditor.graph

import kotlin.Throws
import kotlin.jvm.JvmStatic
import org.jboss.forge.roaster.model.source.JavaClassSource
import org.apache.commons.lang3.StringUtils
import org.semanticweb.owlapi.model.OWLOntologyCreationException
import java.io.File
import java.io.FileWriter
import java.io.IOException
import java.util.Arrays
import java.util.Locale
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

        // IMPORTANT: Only OWL/XML and RDF/XML are supported
        var owlInputPath = "resources/urn_webprotege_ontology_e4316a28-d966-4499-bd93-6be721055117.owx"
        if (args.size == 0) {
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
        if (args.size == 5) {
            owlInputPath = args[0]
            packageNameJava = args[1]
            outputBaseJava = checkPath(args[2])
            packageNameGo = args[3]
            outputBaseGo = checkPath(args[4])
        } else if (args.size == 4) {
            owlInputPath = args[0]
            packageNameJava = args[1]
            outputBaseJava = checkPath(args[2])
            packageNameGo = args[3]
        } else if (args.size == 3) {
            owlInputPath = args[0]
            packageNameJava = args[1]
            outputBaseJava = checkPath(args[2])
        } else if (args.size == 2) {
            owlInputPath = args[0]
            packageNameJava = args[1]
        } else if (args.size == 1) {
            owlInputPath = args[0]
        }
        val owl3 = OWLCloudOntology(owlInputPath)

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

        // Add copyright
        goSourceCode += """// Copyright 2021 Fraunhofer AISEC
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

        // Add package name
        goSourceCode += """
             package ${goSource.packageName}
             
             
             """.trimIndent()

        // Add struct
        goSourceCode += """type ${goSource.name} struct {
"""

        // Check if parentClass exists
        val parentClassName = getParentClassName(goSource.parentClass)
        if (parentClassName != "") goSourceCode += "\t*" + getParentClassName(goSource.parentClass)

        // Add object properties
        goSourceCode += getObjectPropertiesForGoSource(goSource.objectProperties)

        // Add data properties
        goSourceCode += getDataPropertiesForGoSource(goSource.dataProperties)
        goSourceCode += "\n}\n\n"

        // Create needed method for interface
        if (owl3.interfaceList.contains(goSource.parentClass)) {
                goSourceCode += getInterfaceMethod(goSource)
        }

        // TODO: Do we need that anymore?
        // Create new function
        //goSourceCode += getNewFunctionForObjectProperties(goSource);
        return goSourceCode
    }

    private fun getInterfaceMethod(gs: GoStruct): String {

        var receiverType = gs.name
        var receiverChar = receiverType.first().lowercaseChar()
        var interfaceMethodName = gs.parentClass
        var interfaceMethodReturnType = gs.parentClass

        return "func ($receiverChar $receiverType) Get$interfaceMethodName() *$interfaceMethodReturnType{ \n\treturn $receiverChar.$interfaceMethodReturnType\n}"

    }

    // TODO: Do we need that anymore?
//    private fun getNewFunctionForObjectProperties(gs: GoStruct): String {
//        var newFunctionString = ""
//        newFunctionString += "func New" + gs.name + "("
//        for (property in gs.dataProperties) {
//            property.propertyType?.let {
//                newFunctionString += property.propertyName + " " + getGoType(it) + ", "
//            }
//        }
//
//        // Delete last `,` if exists and close )
//        if (newFunctionString[newFunctionString.length - 2] == ',') {
//            newFunctionString = newFunctionString.substring(0, newFunctionString.length - 2) + ") "
//        } else {
//            newFunctionString += ") "
//        }
//        newFunctionString += """
//            *${gs.name}{
//
//            """.trimIndent()
//        newFunctionString += """	return &${gs.name}{
//"""
//        for (property in gs.objectProperties) {
//            newFunctionString += """		${StringUtils.capitalize(property.propertyName)}: TODO get propertyName stuff,
//"""
//        }
//        for (property in gs.dataProperties) {
//            newFunctionString += """		${StringUtils.capitalize(property.propertyName)}:	${
//                StringUtils.uncapitalize(
//                    property.propertyName
//                )
//            },
//"""
//        }
//        newFunctionString += "\t}\n}"
//        return newFunctionString
//    }

    // Change property type to GO type
    private fun getGoType(type: String): String {
        var goType = ""
        goType = when (type) {
            "String" -> "string"
            "float" -> "float32"
            "boolean" -> "bool"
            "java.time.Duration" -> "time.Duration"
            "java.util.Map<String, String>" -> "map[string]string"
            "java.util.ArrayList<Short>" -> "[]int16"
            "java.util.ArrayList<String>" -> "[]string"
            "Short" -> "int16"
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
        if (parentClass == "") return ""
        val parentClassSplit = parentClass.split("\\.").toTypedArray()
        return parentClassSplit[parentClassSplit.size - 1]
    }

    private fun getObjectPropertiesForGoSource(properties: List<Properties>): String {
        var propertiesStringSource = ""
        for (property in properties) {
            propertiesStringSource += if (!property.isRootClassNameResource && !property.isInterface) {
                """
	${StringUtils.capitalize(property.propertyName)}	*""" + StringUtils.capitalize(
                    property.propertyType
                ) + " `json:\"" + property.propertyName + "\"`"
            } else if (!property.isRootClassNameResource && property.isInterface) {
                """
	${StringUtils.capitalize(property.propertyName)}	""" + StringUtils.capitalize(
                    property.propertyType
                ) + " `json:\"" + property.propertyName + "\"`"
            } else {
                // TODO is ResourceID always a slice?
                """
	${StringUtils.capitalize(property.propertyName)}	[]ResourceID `json:"${property.propertyName}"`"""
            }
        }
        return propertiesStringSource
    }

    private fun getDataPropertiesForGoSource(properties: List<Properties>): String {
        var propertiesStringSource = ""
        for (property in properties) {
            property.propertyType?.let {
                propertiesStringSource += """
	${StringUtils.capitalize(property.propertyName)}	${getGoType(it)} `json:"${property.propertyName}"`"""
            }
        }
        return propertiesStringSource
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
        var filenameList: List<String?>? =
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
                fileWriter.write(jcsElem.toString())
                fileWriter.close()
                println("File written to: $filename")
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }
}