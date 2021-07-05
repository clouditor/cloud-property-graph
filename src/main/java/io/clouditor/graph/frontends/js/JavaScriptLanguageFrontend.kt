package io.clouditor.graph.frontends.js

import de.fraunhofer.aisec.cpg.TranslationConfiguration
import de.fraunhofer.aisec.cpg.frontends.LanguageFrontend
import de.fraunhofer.aisec.cpg.graph.NodeBuilder
import de.fraunhofer.aisec.cpg.graph.declarations.TranslationUnitDeclaration
import de.fraunhofer.aisec.cpg.passes.scopes.ScopeManager
import de.fraunhofer.aisec.cpg.sarif.PhysicalLocation
import java.io.File
import org.checkerframework.checker.nullness.qual.NonNull
import org.mozilla.javascript.Parser

class JavaScriptLanguageFrontend(
    config: @NonNull TranslationConfiguration,
    scopeManager: ScopeManager?
) : LanguageFrontend(config, scopeManager, ".") {

    companion object {
        @kotlin.jvm.JvmField var JS_EXTENSIONS: List<String> = listOf(".js")
    }

    val expressionHandler: ExpressionHandler = ExpressionHandler(this)
    val statementHandler: StatementHandler = StatementHandler(this)
    val declarationHandler: DeclarationHandler = DeclarationHandler(this)

    override fun parse(file: File): TranslationUnitDeclaration {
        val parser = Parser()

        val root = parser.parse(file.readText(), file.toURI().toString(), 0)

        val tu = NodeBuilder.newTranslationUnitDeclaration(file.path, getCodeFromRawNode(root))

        scopeManager.resetToGlobal(tu)

        // wrap everything into a virtual global function because we only have declarations on the
        // top
        val func =
            NodeBuilder.newFunctionDeclaration(
                file.nameWithoutExtension + "_global",
                getCodeFromRawNode(root)
            )

        scopeManager.enterScope(func)

        func.body = statementHandler.handle(root)

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
