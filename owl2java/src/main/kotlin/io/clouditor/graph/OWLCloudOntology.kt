package io.clouditor.graph

import org.apache.commons.lang3.StringUtils
import org.jboss.forge.roaster.Roaster
import org.jboss.forge.roaster.model.source.Import
import org.jboss.forge.roaster.model.source.JavaClassSource
import org.jboss.forge.roaster.model.source.MethodSource
import org.jboss.forge.roaster.model.source.PropertySource
import org.semanticweb.owlapi.apibinding.OWLManager
import org.semanticweb.owlapi.model.*
import org.semanticweb.owlapi.model.parameters.Imports
import org.semanticweb.owlapi.search.EntitySearcher
import uk.ac.manchester.cs.owl.owlapi.OWLDataSomeValuesFromImpl
import uk.ac.manchester.cs.owl.owlapi.OWLLiteralImplString
import uk.ac.manchester.cs.owl.owlapi.OWLSubClassOfAxiomImpl
import java.io.File

class OWLCloudOntology(filepath: String) {
    private var ontology: OWLOntology? = null
    private var df: OWLDataFactory? = null

    // Read owl file from filesystem
    @Throws(OWLOntologyCreationException::class)
    private fun readOwlFile(filepath: String) {
        println("Read owl file")
        val manager = OWLManager.createOWLOntologyManager()

        ontology = manager.loadOntologyFromOntologyDocument(File(filepath))
        df = manager.owlDataFactory
    }

    private fun getJavaClassSourceFromOWLClass(clazz: OWLClass): JavaClassSource? {
        var javaClass = Roaster.create(JavaClassSource::class.java)

        // Set class name
        javaClass = setClassName(javaClass, clazz)
        println(
            """
                
                Class [$clazz]  	${getClassName(clazz, ontology)}
                """.trimIndent()
        )
        if (getClassName(clazz, ontology) == "CloudResource") println("")

        // Set package name
        val packageName = "io.clouditor.graph"
        javaClass = javaClass.setPackage(packageName)

        // Set super class name
        javaClass = setSuperClassName(javaClass, clazz)

        // Add constructor shell, need to be here, so that it is the first method
        javaClass = addConstructorShell(javaClass)

        // Set variables by 'OWL object properties'
        javaClass = setOWLClassObjectProperties(javaClass, clazz)

        // Set variables by 'OWL data properties'
        javaClass = setOWLClassDataProperties(javaClass, clazz)

        // Set constructor, superclass constructor is set later, because all class and superclass parameters must
        // be known
        javaClass = setClassConstructor(javaClass)

        // Check syntax
        if (javaClass.hasSyntaxErrors()) {
            System.err.println("SyntaxError: " + javaClass.syntaxErrors)
            return null
        }
        return javaClass
    }

    private fun setClassConstructor(javaClass: JavaClassSource): JavaClassSource {
        // Get method of constructor
        val javaClassConstructor = javaClass.getMethod(javaClass.name)
        if (javaClass.name == "CloudResource") println("")
        if (javaClassConstructor == null) return javaClass

        // Set parameters and body of constructor
        for (elem in javaClass.properties) {
            javaClassConstructor.addParameter(
                elem.type.toString(),
                elem.name
            )
            javaClassConstructor.body = javaClassConstructor.body + elem.mutator.name + "(" + elem.name + ")" +
                    ";"
        }
        return javaClass
    }

    private fun addConstructorShell(javaClass: JavaClassSource): JavaClassSource {
        javaClass.addMethod()
            .setConstructor(true)
            .setBody("")
            .setPublic()
        return javaClass
    }

    // Set OWl class data properties as java class variable
    private fun setOWLClassDataProperties(javaClass: JavaClassSource, clazz: OWLClass): JavaClassSource {
        // Get Set of OWLClassAxioms
        val tempAx = ontology!!.getAxioms(clazz, Imports.EXCLUDED)
        var classRelationshipPropertyName: String
        var classDataPropertyValue: String

        // Currently, it is assumed that there is only one parent, but there can be several relationships
        for (classAxiom in tempAx) {
            val ce = classAxiom as OWLSubClassOfAxiomImpl
            val superClass = ce.superClass

            // If type is DATA_SOME_VALUES_FROM, the 'OWL data property value' is  an literal (string)
            if (superClass.classExpressionType == ClassExpressionType.DATA_SOME_VALUES_FROM) {
                classRelationshipPropertyName = getClassDataPropertyName(superClass)
                classDataPropertyValue = getClassDataPropertyValue(superClass as OWLDataSomeValuesFromImpl)
                if (classDataPropertyValue == "string") classDataPropertyValue =
                    StringUtils.capitalize(classDataPropertyValue)
                javaClass.addProperty(classDataPropertyValue, classRelationshipPropertyName).field.setProtected()
            } else if (superClass.classExpressionType == ClassExpressionType.DATA_HAS_VALUE) {
                // little but hacky,
                classRelationshipPropertyName = getClassDataPropertyName(superClass)
                classDataPropertyValue = getClassDataPropertyValue(superClass as OWLDataHasValue)
                if (classDataPropertyValue == "string") classDataPropertyValue =
                    StringUtils.capitalize(classDataPropertyValue)
                val property =
                    javaClass.addProperty(classDataPropertyValue, classRelationshipPropertyName).field.setProtected()

                // check, if the type is a Map, then we need to ignore it in neo4j for now
                if (classDataPropertyValue.startsWith("java.util.Map")) {
                    property.addAnnotation("org.neo4j.ogm.annotation.Transient")
                }
            }
        }
        return javaClass
    }

    // Set OWl class object properties as java class variable
    private fun setOWLClassObjectProperties(javaClass: JavaClassSource, clazz: OWLClass): JavaClassSource {
        // Get Set of OWLClassAxioms
        val tempAx = ontology!!.getAxioms(clazz, Imports.EXCLUDED)
        var classRelationshipPropertyName: String

        // Currently, it is assumed that there is only one parent, but there can be several relationships
        for (classAxiom in tempAx) {
            val ce = classAxiom as OWLSubClassOfAxiomImpl
            val superClass = ce.superClass

            // If type is OBJECT_SOME_VALUES_FROM it is an 'OWL object property'
            if (superClass.classExpressionType == ClassExpressionType.OBJECT_SOME_VALUES_FROM) {
                classRelationshipPropertyName = getClassObjectPropertyName(superClass)
                val property: PropertySource<JavaClassSource?>? = when (classRelationshipPropertyName) {
                    "has", "offers" -> javaClass.addProperty(
                        formatString(getClassName(superClass, ontology)),
                        decapitalizeString(formatString(getClassName(superClass, ontology)))
                    )
                    "hasMultiple" -> javaClass.addProperty(
                        formatString(getArrayClassName(superClass, ontology)),
                        decapitalizeString(formatString(getPlural(getClassName(superClass, ontology))))
                    )
                    else -> // TODO: store this information in the property itself, i.e. if it is an array or not. for now all are arrays
                        javaClass.addProperty(
                            formatString(getArrayClassName(superClass, ontology)),
                            classRelationshipPropertyName
                        )
                }
                property?.field?.setProtected()
            }
        }
        return javaClass
    }

    private fun getPlural(s: String): String {
        return s + "s"
    }

    private fun decapitalizeString(string: String?): String {
        return if (string == null || string.isEmpty()) "" else Character.toLowerCase(string[0])
            .toString() + string.substring(1)
    }

    private fun setSuperClassName(javaClass: JavaClassSource, clazz: OWLClass): JavaClassSource {
        // Get Set of OWLClassAxioms
        val tempAx = ontology!!.getAxioms(clazz, Imports.EXCLUDED)
        var superClassName = ""

        // Currently, it is assumed that there is only one 'OWL parent', but there can be several 'OWL relationships'
        for (classAxiom in tempAx) {
            val ce = classAxiom as OWLSubClassOfAxiomImpl
            val superClass = ce.superClass

            // If type is OWL_CLASS it is the 'OWL parent'
            if (superClass.classExpressionType == ClassExpressionType.OWL_CLASS) {
                superClassName = getClassName(superClass, ontology)
            }
        }

        // Format super class name
        superClassName = formatString(superClassName)
        /*if (javaClass.name == "HttpRequest") {
            javaClass.superType = "de.fraunhofer.aisec.cpg.graph.declarations.FunctionDeclaration"
        } else*/ if (!superClassName.isEmpty()) {
            javaClass.superType = superClassName
        } else {
            javaClass.superType = "de.fraunhofer.aisec.cpg.graph.Node"
        }
        return javaClass
    }

    // Deletes not needed characters from string, e.g. space, '/', '-'
    private fun formatString(unformattedString: String): String {
        var s = unformattedString

        if (s.contains(" ")) s = s.replace(" ", "")
        if (s.contains("/")) s = s.replace("/", "")
        if (s.contains("-")) s = s.replace("-", "")

        return s
    }

    private fun setClassName(javaClass: JavaClassSource, clazz: OWLClass): JavaClassSource {
        var className = getClassName(clazz, ontology)

        // Format class name
        if (className.contains("#")) className = className.split("#").toTypedArray()[1]
        className = formatString(className)
        javaClass.setName(className)
            .setPublic()
        return javaClass
    }

    // Set superclass call, must be done here to have the parameters from the superclass constructor
    // Get all java classes from OWL file
    val javaClassSources: List<JavaClassSource>
        get() {
            val classes = ontology!!.classesInSignature
            var jcsList: MutableList<JavaClassSource> = ArrayList()
            for (clazz in classes) {
                // skip owl:Thing
                if (clazz.isOWLThing) {
                    continue
                }
                val jcs = getJavaClassSourceFromOWLClass(clazz)
                if (jcs != null) jcsList.add(jcs)
            }

            // Set superclass call, must be done here to have the parameters from the superclass constructor
            jcsList = addSuperclassProperties(jcsList)
            return jcsList
        }

    // Add superclass imports and add superclass call
    private fun addSuperclassProperties(jcsList: MutableList<JavaClassSource>): MutableList<JavaClassSource> {
        for (jcs in jcsList) {

            // If superclass is 'Node', do nothing
            if (StringUtils.substringAfterLast(jcs.superType, ".") == "Node") continue
            // HCKY HACK HACK
            if (jcs.name == "HttpRequest") {
                continue
            }

            // Set superclass call
            addSuperclassCall(jcs, jcsList)

            // Add imports from superclasses to javaclass
            addImportsFromSuperclass(jcs, jcsList)
        }

        return jcsList
    }

    private fun setEmptySuperclassCall(jcs: JavaClassSource): JavaClassSource {
        val javaClassConstructor =
            jcs.methods.stream().filter { a: MethodSource<JavaClassSource?> -> a.name == jcs.name }
                .findFirst().get()
        javaClassConstructor.body = "super();"
        return jcs
    }

    private fun addSuperclassCall(
        jcs: JavaClassSource,
        jcsList: List<JavaClassSource>
    ): JavaClassSource {
        // Get the superClass object
        var superClass: JavaClassSource? = null
        for (elem in jcsList) {
            if (elem.name == StringUtils.substringAfterLast(jcs.superType, ".")) {
                return if (StringUtils.substringAfterLast(elem.name, ".") != "Node") {
                    superClass = elem
                    break
                } else {
                    jcs
                }
            }
        }

        // If superclass is 'Node' set emtpy superclass call
        if (superClass!!.name == "Node") {
            setEmptySuperclassCall(jcs)
            return jcs
        }

        // Get all parameters of superClasses
        var superXClassParameters: MutableMap<String, String?> = LinkedHashMap()
        superXClassParameters = getSuperXClassParameters(superClass, jcsList, superXClassParameters)

        // Get javaClass constructor
        val javaClassConstructor =
            jcs.methods.stream().filter { a: MethodSource<JavaClassSource?> -> a.name == jcs.name }
                .findFirst().get()

        // Add parameters of superclass to javaClass constructor
        for ((key, value) in superXClassParameters) {
            javaClassConstructor.addParameter(value, key)
        }

        // Add 'super(superClassParameter1, superClassParameter2, ...)' to javaclass constructor body
        javaClassConstructor.body = "super(" +
                getListAsCommaSeparatedString(superXClassParameters) + ");" + javaClassConstructor.body
        return jcs
    }

    private fun addImportsFromSuperclass(jcs: JavaClassSource, jcsList: List<JavaClassSource>): JavaClassSource {
        // TODO use only necessary imports
        var superXClassImports: MutableList<Import?> = ArrayList()
        superXClassImports = getSuperXClassImports(jcs, jcsList, superXClassImports)
        for (elem in superXClassImports) {
            jcs.addImport(elem)
        }

        return jcs
    }

    private fun getSuperXClassImports(
        jcs: JavaClassSource, jcsList: List<JavaClassSource>,
        superXClassImports: MutableList<Import?>
    ): MutableList<Import?> {
        for (elem in jcs.imports) {
            superXClassImports.add(elem)
        }
        return if (StringUtils.substringAfterLast(jcs.superType, ".") == "Node") superXClassImports else {
            getSuperXClassImports(
                jcsList.stream()
                    .filter { a: JavaClassSource -> a.name == StringUtils.substringAfterLast(jcs.superType, ".") }
                    .findFirst().get(), jcsList, superXClassImports)
        }
    }

    // Get a Map<name, type> of superClassParameters
    private fun getSuperXClassParameters(
        jcs: JavaClassSource?,
        jcsList: List<JavaClassSource>,
        superXClassParameters: MutableMap<String, String?>
    ): MutableMap<String, String?> {
        for (elem in jcs!!.properties) {
            superXClassParameters[elem.name] = elem.type.toString()
        }
        return if (StringUtils.substringAfterLast(jcs.superType, ".") == "Node") superXClassParameters else {
            getSuperXClassParameters(jcsList.stream().filter { a: JavaClassSource ->
                a.name == StringUtils.substringAfterLast(
                    jcs.superType, "."
                )
            }.findFirst().get(), jcsList, superXClassParameters)
        }
    }

    private fun getListAsCommaSeparatedString(superClassParameters: Map<String, String?>): String {
        var superClassParametersAsCommaSeparatedString = ""

        for ((key) in superClassParameters) {
            if (superClassParametersAsCommaSeparatedString != "") superClassParametersAsCommaSeparatedString =
                "$superClassParametersAsCommaSeparatedString,"
            superClassParametersAsCommaSeparatedString += key
        }

        return superClassParametersAsCommaSeparatedString
    }

    private fun getArrayClassName(nce: OWLClassExpression, ontology: OWLOntology?): String {
        return "java.util.List<" + getClassName(nce, ontology) + ">"
    }

    // Get class name from OWLClassExpression
    private fun getClassName(nce: OWLClassExpression, ontology: OWLOntology?): String {
        for (elem in nce.classesInSignature) {
            for (item in EntitySearcher.getAnnotationObjects(elem, ontology!!)) {
                if (item != null) {
                    if (item.property.iri.remainder.get() == "label") {
                        return if (item.value.toString().contains("\"")) item.value.toString().split("\"")
                            .toTypedArray()[1] else (item.value as OWLLiteralImplString).literal
                    }
                }
            }
        }
        return ""
    }

    // Get class data property value (relationship in OWL)
    private fun getClassDataPropertyValue(nce: OWLDataHasValue): String {
        return nce.filler.toString().split(":").toTypedArray()[1].replace("\"", "")
    }

    private fun getClassDataPropertyValue(nce: OWLDataSomeValuesFrom): String {
        return nce.filler.toString().split(":").toTypedArray()[1]
    }

    // Get class data property name (realtionship in OWL)
    private fun getClassDataPropertyName(nce: OWLClassExpression): String {
        for (elem in nce.dataPropertiesInSignature) {
            for (item in EntitySearcher.getAnnotationObjects(elem, ontology!!)) {
                if (item != null) {
                    return item.value.toString().split("\"").toTypedArray()[1]
                }
            }
        }
        return ""
    }

    // Get class object property name (relationship in owl)
    private fun getClassObjectPropertyName(nce: OWLClassExpression): String {
        for (elem in nce.objectPropertiesInSignature) {
            for (item in EntitySearcher.getAnnotationObjects(elem, ontology!!)) {
                if (item != null) {
                    return item.value.toString().split("\"").toTypedArray()[1]
                }
            }
        }
        return ""
    }

    init {
        readOwlFile(filepath)
    }
}