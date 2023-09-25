package io.clouditor.graph.passes

import de.fraunhofer.aisec.cpg.TranslationContext
import de.fraunhofer.aisec.cpg.passes.TranslationResultPass

abstract class CloudResourceDiscoveryPass(ctx: TranslationContext) : TranslationResultPass(ctx) {}
