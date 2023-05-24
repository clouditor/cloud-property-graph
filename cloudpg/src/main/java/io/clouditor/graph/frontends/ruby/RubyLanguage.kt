package io.clouditor.graph.frontends.ruby

import de.fraunhofer.aisec.cpg.ScopeManager
import de.fraunhofer.aisec.cpg.TranslationConfiguration
import de.fraunhofer.aisec.cpg.frontends.Language
import kotlin.reflect.KClass

class RubyLanguage : Language<RubyLanguageFrontend>() {
    override val fileExtensions: List<String>
        get() = listOf(".rb")
    override val frontend: KClass<out RubyLanguageFrontend>
        get() = RubyLanguageFrontend::class
    override val namespaceDelimiter: String
        get() = "::"

    override fun newFrontend(
        config: TranslationConfiguration,
        scopeManager: ScopeManager
    ): RubyLanguageFrontend {
        return RubyLanguageFrontend(this, config, scopeManager)
    }
}
