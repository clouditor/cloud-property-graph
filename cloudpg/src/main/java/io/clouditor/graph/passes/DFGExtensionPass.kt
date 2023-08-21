package io.clouditor.graph.passes

import de.fraunhofer.aisec.cpg.TranslationContext
import de.fraunhofer.aisec.cpg.TranslationResult
import de.fraunhofer.aisec.cpg.graph.HasType
import de.fraunhofer.aisec.cpg.graph.declarations.FieldDeclaration
import de.fraunhofer.aisec.cpg.graph.statements.expressions.CallExpression
import de.fraunhofer.aisec.cpg.graph.statements.expressions.KeyValueExpression
import de.fraunhofer.aisec.cpg.graph.statements.expressions.MemberExpression
import de.fraunhofer.aisec.cpg.graph.types.ObjectType
import de.fraunhofer.aisec.cpg.graph.types.Type
import de.fraunhofer.aisec.cpg.helpers.SubgraphWalker
import de.fraunhofer.aisec.cpg.passes.GoExtraPass
import de.fraunhofer.aisec.cpg.passes.TranslationResultPass
import de.fraunhofer.aisec.cpg.passes.order.DependsOn
import io.clouditor.graph.plusAssign
import java.util.*

/**
 * The purpose of this Pass os to enhance the DFG graph with additional edges that are needed for
 * DF-Label tracking. One such case is the labeling of a variable that is the base of a member
 * expression. Normally no data flows from the base to the member expression. FOr this use case,
 * however, the mere usage of a base causes labels to be relevant for the member expression.
 */
@DependsOn(GoExtraPass::class)
class DFGExtensionPass(ctx: TranslationContext) : TranslationResultPass(ctx) {

    override fun accept(t: TranslationResult) {
        // loop through services
        val nodes = SubgraphWalker.flattenAST(t)
        val memberExpressions = nodes.filterIsInstance<MemberExpression>()

        val stringifyFunctions =
            nodes.filterIsInstance<CallExpression>().filter { node ->
                node.name.localName == "stringify"
            }
        // val stringFunctions: List<CallExpression>  =
        // nodes.filterIsInstance<CallExpression>().filter { node -> node.name == "stringify" ||
        // node.name == "toString" }

        val keyValueExpressions: List<KeyValueExpression> =
            nodes.filterIsInstance<KeyValueExpression>()
        connectDFGValuesToKeyValueExpression(keyValueExpressions)

        stringifyFunctions.forEach {
            redirectDFGThroughFunctionCall(it)
            drawDFGEgesFromNestedFields(it)
        }

        memberExpressions.forEach { it.addPrevDFG(it.base) }
    }

    /**
     * Adds A DFG Edge from the value of a Key Value Expression to the Expression. This is to follow
     * unpacking operations although there is no explicit data flow through the key Value expression
     * to for example an InitializerListExpression
     */
    private fun connectDFGValuesToKeyValueExpression(
        keyValueExpressions: List<KeyValueExpression>
    ) {
        keyValueExpressions.forEach {
            val keyValueExpression: KeyValueExpression = it
            it.value?.let { keyValueExpression.addPrevDFG(it) }
        }
    }

    private fun redirectDFGThroughFunctionCall(call: CallExpression) {
        call.arguments.forEach { call.addPrevDFG(it) }
    }

    private fun drawDFGEgesFromNestedFields(call: CallExpression) {
        call.arguments.forEach { it ->
            val nestedFields: MutableSet<FieldDeclaration> = getNestedFields(it)
            nestedFields.forEach { call.addPrevDFG(it) }
        }
    }

    private fun dereferenceToObjectType(originalType: Type): ObjectType? {
        var type: Type = originalType
        var derefType: Type = type.dereference()
        while (!Objects.equals(type, derefType) || type is ObjectType) {
            val tmp = derefType
            derefType = type.dereference()
            type = tmp
        }
        return type as? ObjectType
    }

    private fun getNestedFields(
        node: HasType,
        visitedfields: MutableSet<FieldDeclaration> = mutableSetOf()
    ): MutableSet<FieldDeclaration> {
        var fields: MutableSet<FieldDeclaration> = mutableSetOf()
        node.possibleSubTypes.map { it ->
            val oType: ObjectType? = dereferenceToObjectType(it)
            oType?.let {
                fields = it.recordDeclaration!!.fields.toMutableSet()
                if (!visitedfields.addAll(fields)) {
                    return visitedfields
                }
            }
        }

        fields.forEach {
            it.prevDFG.filterIsInstance<HasType>().forEach { getNestedFields(it, visitedfields) }
        }

        return fields
    }

    override fun cleanup() {}
}
