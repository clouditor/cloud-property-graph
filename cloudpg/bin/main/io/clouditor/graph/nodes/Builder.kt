package io.clouditor.graph.nodes

import de.fraunhofer.aisec.cpg.graph.Node
import io.clouditor.graph.Image

/**
 * A builder represents something that builds an image or an application, i.e. a GitHub workflow.
 */
class Builder(val builds: MutableList<Image> = mutableListOf()) : Node()
