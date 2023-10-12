package io.clouditor.graph.frontends.ruby

import de.fraunhofer.aisec.cpg.TranslationContext
import de.fraunhofer.aisec.cpg.frontends.Language
import de.fraunhofer.aisec.cpg.frontends.LanguageFrontend
import de.fraunhofer.aisec.cpg.graph.Node
import de.fraunhofer.aisec.cpg.graph.declarations.TranslationUnitDeclaration
import de.fraunhofer.aisec.cpg.graph.newFunctionDeclaration
import de.fraunhofer.aisec.cpg.graph.newTranslationUnitDeclaration
import de.fraunhofer.aisec.cpg.graph.types.Type
import de.fraunhofer.aisec.cpg.sarif.PhysicalLocation
import java.io.File
import org.checkerframework.checker.nullness.qual.NonNull
import org.jruby.Ruby
import org.jruby.ast.BlockNode
import org.jruby.ast.RootNode
import org.jruby.parser.Parser
import org.jruby.parser.ParserConfiguration

class RubyLanguageFrontend(
    language: Language<RubyLanguageFrontend>,
    ctx: @NonNull TranslationContext
) : LanguageFrontend<Node, Type>(language, ctx) {
    val declarationHandler: DeclarationHandler = DeclarationHandler(this)
    val expressionHandler: ExpressionHandler = ExpressionHandler(this)
    val statementHandler: StatementHandler = StatementHandler(this)

    override fun parse(file: File): TranslationUnitDeclaration {
        val ruby = Ruby.getGlobalRuntime()
        val parser = Parser(ruby)

        val node =
            parser.parse(
                file.path,
                file.inputStream(),
                null,
                ParserConfiguration(ruby, 0, false, true, false)
            ) as
                RootNode

        return handleRootNode(node, file)
    }

    override fun codeOf(astNode: Node): String {
        return ""
    }

    override fun locationOf(astNode: Node): PhysicalLocation? {
        return null
    }

    override fun typeOf(type: Type): Type {
        TODO("Not yet implemented")
    }

    override fun setComment(node: Node, astNode: Node) {}

    private fun handleRootNode(node: RootNode, file: File): TranslationUnitDeclaration {
        val tu = newTranslationUnitDeclaration(node.file, codeOf(node))

        scopeManager.resetToGlobal(tu)

        // wrap everything into a virtual global function because we only have declarations on the
        // top
        val func =
            newFunctionDeclaration(file.nameWithoutExtension + "_global", codeOf(node))

        scopeManager.enterScope(func)

        func.body = statementHandler.handle(node.bodyNode as BlockNode)

        scopeManager.leaveScope(func)

        scopeManager.addDeclaration(func)

        return tu
    }
}
