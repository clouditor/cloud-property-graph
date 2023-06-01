package io.clouditor.graph.frontends.ruby

import de.fraunhofer.aisec.cpg.TranslationConfiguration
import de.fraunhofer.aisec.cpg.frontends.LanguageFrontend
import de.fraunhofer.aisec.cpg.graph.NodeBuilder
import de.fraunhofer.aisec.cpg.graph.declarations.TranslationUnitDeclaration
import de.fraunhofer.aisec.cpg.ScopeManager
import de.fraunhofer.aisec.cpg.sarif.PhysicalLocation
import java.io.File
import org.checkerframework.checker.nullness.qual.NonNull
import org.jruby.Ruby
import org.jruby.ast.BlockNode
import org.jruby.ast.RootNode
import org.jruby.parser.Parser
import org.jruby.parser.ParserConfiguration

// FIXME: inheritance from LanguageFrontend
class RubyLanguageFrontend(config: @NonNull TranslationConfiguration, scopeManager: ScopeManager?) :
    LanguageFrontend(config, scopeManager, "::") {

    companion object {
        @kotlin.jvm.JvmField var RUBY_EXTENSIONS: List<String> = listOf(".rb")
    }

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

    private fun handleRootNode(node: RootNode, file: File): TranslationUnitDeclaration {
        // FIXME: NodeBuilder
        val tu = NodeBuilder.newTranslationUnitDeclaration(node.file, getCodeFromRawNode(node))

        scopeManager.resetToGlobal(tu)

        // wrap everything into a virtual global function because we only have declarations on the
        // top
        val func =
            NodeBuilder.newFunctionDeclaration(
                file.nameWithoutExtension + "_global",
                getCodeFromRawNode(node)
            )

        scopeManager.enterScope(func)

        func.body = statementHandler.handle(node.bodyNode as BlockNode)

        scopeManager.leaveScope(func)

        scopeManager.addDeclaration(func)

        return tu
    }

    override fun <T : Any?> getCodeFromRawNode(astNode: T): String? {
        return ""
    }

    override fun <T : Any?> getLocationFromRawNode(astNode: T): PhysicalLocation? {
        return null
    }

    override fun <S : Any?, T : Any?> setComment(s: S, ctx: T) {}
}
