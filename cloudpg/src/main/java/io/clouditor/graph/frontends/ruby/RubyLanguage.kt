package io.clouditor.graph.frontends.ruby

import de.fraunhofer.aisec.cpg.ScopeManager
import de.fraunhofer.aisec.cpg.frontends.*
import de.fraunhofer.aisec.cpg.graph.Name
import de.fraunhofer.aisec.cpg.graph.declarations.RecordDeclaration
import de.fraunhofer.aisec.cpg.graph.statements.expressions.MemberExpression
import de.fraunhofer.aisec.cpg.graph.types.*
import kotlin.reflect.KClass

/** The Ruby Language */
class RubyLanguage() :
    Language<RubyLanguageFrontend>(),
    HasDefaultArguments,
    HasClasses,
    HasSuperClasses,
    HasShortCircuitOperators {
    override val fileExtensions = listOf("rb")
    override val namespaceDelimiter = "::"
    @Transient override val frontend: KClass<out RubyLanguageFrontend> = RubyLanguageFrontend::class
    override val superClassKeyword = "super"
    override val conjunctiveOperators = listOf("&&")
    override val disjunctiveOperators = listOf("||")

    @Transient
    /** See [The RubySpec](https://github.com/ruby/spec) */
    override val builtInTypes =
        mapOf(
            // The bit width of the Integer type in Ruby is only limited by your memory
            "Integer" to IntegerType("Integer", null, this, NumericType.Modifier.SIGNED),
            "Float" to FloatingPointType("Float", 64, this, NumericType.Modifier.SIGNED),
            "String" to StringType("String", this),
            // The bit width of Booleans is not defined in the specification and
            // implementation-dependant
            "Boolean" to BooleanType("Boolean", null, this, NumericType.Modifier.NOT_APPLICABLE)
        )

    override val compoundAssignmentOperators =
        setOf(
            "+=", // Addition assignment
            "-=", // Subtraction assignment
            "*=", // Multiplication assignment
            "/=", // Division assignment
            "%=", // Modulo assignment
            "**=", // Exponentiation assignment
            "<<=", // Left shift assignment
            ">>=", // Right shift assignment
            "&=", // Bitwise AND assignment
            "|=", // Bitwise OR assignment
            "^=" // Bitwise XOR assignment
        )

    override fun handleSuperCall(
        callee: MemberExpression,
        curClass: RecordDeclaration,
        scopeManager: ScopeManager,
        recordMap: Map<Name, RecordDeclaration>
    ): Boolean {
        TODO("Not yet implemented")
    }
}
