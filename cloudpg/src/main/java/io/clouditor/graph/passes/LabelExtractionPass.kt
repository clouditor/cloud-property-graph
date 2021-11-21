package io.clouditor.graph.passes

import de.fraunhofer.aisec.cpg.TranslationResult
import de.fraunhofer.aisec.cpg.graph.Annotation
import de.fraunhofer.aisec.cpg.graph.Node
import de.fraunhofer.aisec.cpg.graph.declarations.Declaration
import de.fraunhofer.aisec.cpg.graph.declarations.FunctionDeclaration
import de.fraunhofer.aisec.cpg.graph.declarations.RecordDeclaration
import de.fraunhofer.aisec.cpg.graph.statements.DeclarationStatement
import de.fraunhofer.aisec.cpg.graph.statements.ReturnStatement
import de.fraunhofer.aisec.cpg.graph.statements.expressions.Expression
import de.fraunhofer.aisec.cpg.helpers.SubgraphWalker
import de.fraunhofer.aisec.cpg.passes.Pass
import io.clouditor.graph.nodes.labels.Identifier
import io.clouditor.graph.nodes.labels.Label
import io.clouditor.graph.nodes.labels.PrivacyLabel
import io.clouditor.graph.nodes.labels.PseudoIdentifier
import io.clouditor.graph.plusAssign
import java.util.function.Consumer
import java.util.function.Predicate
import java.util.stream.Collectors

class LabelExtractionPass : Pass() {

    val predicatesToHandle: MutableMap<Predicate<Node>, Consumer<Node>> = mutableMapOf()

    var edgesCachePass: BidirectionalEdgesCachePass? = null

    init {
        // Todo Here you can add other predicates to add labels
    }

    override fun accept(t: TranslationResult) {
        // loop through services
        val nodes = SubgraphWalker.flattenAST(t)

        // Register default extractor that gets Label from Annotation
        predicatesToHandle.put(
            { node -> node.annotations.count() > 0 },
            { node -> handleAnnotations(t, node) }
        )

        // Register default extractor that gets Label from annotation like comment content
        predicatesToHandle.put(
            { node -> node.comment != null && node.comment!!.isNotEmpty() },
            { node -> handleComment(t, node) }
        )

        nodes.forEach { node: Node ->
            predicatesToHandle.forEach { predicate, handler ->
                if (predicate.test(node)) {
                    handler.accept(node)
                }
            }
        }
    }

    /**
     * Extracts labels from Annotations of name "PrivacyLabel" including the attribute of a privacy
     * level. Edges are attached to the DFG-Border nodes. Nodes that are in the Sub-AST of the
     * annotated node, and have an outgoing DFG-edge to another node not in the annotated nodes
     * Sub-AST
     */
    private fun handleAnnotations(t: TranslationResult, annotationParent: Node) {
        annotationParent.annotations.forEach {
            var label: Label? = null
            when (it.name) {
                "PrivacyLabel" -> label = handlePrivacyLabelAnnotation(annotationParent, it)
                "Identifier" -> label = handleIdentifierAnnotation(annotationParent, it)
                "PseudoIdentifier" -> label = handlePseudoIdentifierAnnotation(annotationParent, it)
            }
            label?.let { t += it } // Adding Labels to the supplementary nodes of a translation unit
        }
    }

    /**
     * Extracts labels from Annotations of name "PrivacyLabel" including the attribute of a privacy
     * level. Edges are attached to the DFG-Border nodes. Nodes that are in the Sub-AST of the
     * annotated node, and have an outgoing DFG-edge to another node not in the annotated nodes
     * Sub-AST
     */
    private fun handleComment(t: TranslationResult, nodeWComment: Node) {
        var regexes =
            mutableMapOf(
                Regex("@Identifier($|\\s)") to this::handleIdentifierComments,
                Regex("@PrivacyLabel\\(level=([0-9]+)\\)($|\\s)") to
                    this::handlePrivacyLabelComments,
                Regex("@PseudoIdentifier(\$|\\s)") to this::handlePseudoIdentifierComments,
            )

        regexes.entries.forEach {
            val matches = it.key.findAll(nodeWComment.comment!!)
            if (matches.toList().isNotEmpty()) {
                var labels = it.value(nodeWComment, matches)
                labels.forEach {
                    t += it // Adding Labels to the supplementary nodes of a translation unit
                }
            }
        }
    }

    private fun handleIdentifierComments(node: Node, matches: Sequence<MatchResult>): List<Label> {
        return listOf(labelCreationDispatcher<Identifier>(node))
    }

    private fun handlePseudoIdentifierComments(
        node: Node,
        matches: Sequence<MatchResult>
    ): List<Label> {
        return listOf(labelCreationDispatcher<PseudoIdentifier>(node))
    }

    private fun handlePrivacyLabelComments(
        node: Node,
        matches: Sequence<MatchResult>
    ): List<Label> {
        var labels: MutableList<Label> = mutableListOf()
        matches.forEach {
            val thisLevel: Int? = it.destructured.toList().first().toIntOrNull()
            thisLevel?.let {
                val label = labelCreationDispatcher<PrivacyLabel>(node)
                label.protectionlevel = it
                labels.add(label)
            }
        }
        return labels
    }

    private fun handlePrivacyLabelAnnotation(
        annotationParent: Node,
        annotation: Annotation
    ): Label? {
        val values: MutableList<Expression> =
            annotation
                .members
                .filter { member -> member.name == "level" }
                .map { member -> member.value }
                .toMutableList()
        // This is to handle annotation that don't use named attributes, e.g. decorators in
        // TypeScript that are
        // more of meta calls to functions
        if (values.isEmpty() && !annotation.members.isEmpty()) {
            values.add(annotation.members.get(0).value)
        }
        if (!values.isEmpty()) {
            val param: Node? = values.get(0) as? Node
            param?.let {
                val label = labelCreationDispatcher<PrivacyLabel>(annotationParent)

                label.protectionlevel = it.code!!.toIntOrNull() ?: 0
                return label
            }
        }
        return null
    }

    /**
     * Creates an Identifier label connected to the given node or its dataflows identified by the
     * rules in the {@link labelCreationDispatcher}.
     */
    private fun handleIdentifierAnnotation(
        annotationParent: Node,
        annotation: Annotation
    ): Identifier? {
        return labelCreationDispatcher(annotationParent)
    }

    /**
     * Creates an Identifier label connected to the given node or its dataflows identified by the
     * rules in the {@link labelCreationDispatcher}.
     */
    private fun handlePseudoIdentifierAnnotation(
        annotationParent: Node,
        annotation: Annotation
    ): PseudoIdentifier? {
        return labelCreationDispatcher(annotationParent)
    }

    inline fun <reified T : Label> labelCreationDispatcher(node: Node): T {
        var label: T = initLabel<T>(node)
        when (node) {
            is FunctionDeclaration -> {
                addLabelToDFGBorderEdges(node, label)
                // addLabelToReturnedExpressions(node,label)
                // Todo or to everything that is not a subcall to catch returns and writes to pass
                // by reference objects
            }
            is RecordDeclaration -> {
                addLabelToDFGBorderEdges(node, label)
                addLabelToInstantiations(node, label)
            }
            is DeclarationStatement -> {
                addLabelToDFGBorderEdges(node, label)
            }
            else -> {
                addLabelToDFGBorderEdges(node, label)
            }
        }
        return label
    }

    /**
     * Function to just add labels to the annotated node, the type of Label can me specified through
     * the generic type specialization. Nodes that are in the Sub-AST of the annotated node, and
     * have an outgoing DFG-edge to another node not in the annotated nodes Sub-AST.
     */
    inline fun <reified T : Label> addLabelToAnnotatedNode(n: Node): T {
        var label: T = T::class.constructors.first().call(n)
        label.labeledNodes.add(n)
        return label
    }

    /** Adds a newly created Label to the DFG-Border nodes, */
    fun addLabelToDFGBorderEdges(n: Node, label: Label) {

        var dfgExitNodes: MutableList<Node> = getDFGPathEdges(n)!!.exits

        label.labeledNodes.addAll(dfgExitNodes)
    }

    /**
     * Labels are added to the dfg-edges that leave Nodes that create the given declaration. This is
     * such that we can add a label to all object creations of a sensitive Class.
     */
    fun addLabelToInstantiations(n: Node, label: Label) {
        if (n is Declaration) {
            edgesCachePass?.getEdgeSourceOf(n, BidirectionalEdgesCachePass.EdgeLabel.INSTANTIATES)
                ?.forEach { addLabelToDFGBorderEdges(it, label) }
        }
    }

    inline fun <reified T : Label> initLabel(n: Node): T {
        return T::class.constructors.first().call(n)
    }

    /** Adds a newly created Label to the DFG-Border nodes, */
    fun addLabelToReturnedExpressions(n: Node, label: Label) {

        val returns: List<ReturnStatement> =
            SubgraphWalker.flattenAST(n).filterIsInstance<ReturnStatement>()

        label.labeledNodes.addAll(returns)
    }

    /**
     * Function returns two lists in a list. The first list contains all dfg nodes with no
     * predecesor in the subgraph with root 'n'. The second list contains dfg edges that have no
     * successor in the subgraph with root 'n'. The first List marks the entry and the second marks
     * the exit nodes of the dfg in this subgraph.
     *
     * @param n
     * - root of the subgraph.
     * @return Two lists, list 1 contains all dfg entries and list 2 contains all exits.
     */
    fun getDFGPathEdges(n: Node?): SubgraphWalker.Border? {
        var border = SubgraphWalker.Border()
        var flattedASTTree = SubgraphWalker.flattenAST(n)
        var dfgNodes =
            flattedASTTree
                .stream()
                .filter { node: Node -> !node.prevDFG.isEmpty() || !node.nextDFG.isEmpty() }
                .collect(Collectors.toList())
        // Nodes that are incoming edges, no other node
        border.entries.addAll(
            dfgNodes
                .stream()
                .filter { node: Node ->
                    node.prevDFG.stream().anyMatch { prev: Node -> !dfgNodes.contains(prev) }
                }
                .collect(Collectors.toList())
        )
        border.exits.addAll(
            dfgNodes
                .stream()
                .filter { node: Node ->
                    node.nextDFG.stream().anyMatch { next: Node -> !dfgNodes.contains(next) }
                }
                .collect(Collectors.toList())
        )
        return border
    }

    override fun cleanup() {}
}
