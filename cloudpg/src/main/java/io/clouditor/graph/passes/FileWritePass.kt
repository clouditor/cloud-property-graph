package io.clouditor.graph.passes

import de.fraunhofer.aisec.cpg.TranslationResult
import de.fraunhofer.aisec.cpg.graph.statements.expressions.CallExpression
import de.fraunhofer.aisec.cpg.passes.Pass
import io.clouditor.graph.Application
import io.clouditor.graph.FileWrite
import io.clouditor.graph.plusAssign

abstract class FileWritePass: Pass() {

    protected fun createFileWrite(
        t: TranslationResult,
        call: CallExpression,
        app: Application?
    ): FileWrite {
        // Create node
        val fileWriteNode = FileWrite(call)
        // Add to functionalities if necessary
        app?.functionalities?.plusAssign(fileWriteNode)
        // Add to translation result
        t += fileWriteNode
        return fileWriteNode
    }
}