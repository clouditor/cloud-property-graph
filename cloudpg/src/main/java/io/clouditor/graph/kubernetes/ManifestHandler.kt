package io.clouditor.graph.kubernetes

import de.fraunhofer.aisec.cpg.TranslationResult
import io.clouditor.graph.Application
import io.clouditor.graph.additionalNodes
import java.nio.file.Path

class ManifestHandler(private val result: TranslationResult, val rootPath: Path) {

    // check if there is an ingress defined and add its host path to the application's compute resources
    fun handleManifest(manifest: Manifest) {
        if (manifest.kind == "Ingress") {
            val name = manifest.metadata?.name
            val host = manifest.spec?.rules?.filter { it.host != null }?.firstOrNull()
            val application =
                result.additionalNodes.filterIsInstance(Application::class.java).firstOrNull {
                    it.name == name
                }
            application?.runsOn?.firstOrNull()?.labels?.set("host", host?.host)
        }
    }
}
