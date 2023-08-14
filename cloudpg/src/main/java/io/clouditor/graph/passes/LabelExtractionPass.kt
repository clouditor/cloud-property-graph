package io.clouditor.graph.passes

import de.fraunhofer.aisec.cpg.TranslationContext
import de.fraunhofer.aisec.cpg.TranslationResult
import de.fraunhofer.aisec.cpg.graph.Annotation
import de.fraunhofer.aisec.cpg.graph.Node
import de.fraunhofer.aisec.cpg.graph.declarations.Declaration
import de.fraunhofer.aisec.cpg.graph.declarations.FunctionDeclaration
import de.fraunhofer.aisec.cpg.graph.declarations.RecordDeclaration
import de.fraunhofer.aisec.cpg.graph.declarations.VariableDeclaration
import de.fraunhofer.aisec.cpg.graph.statements.DeclarationStatement
import de.fraunhofer.aisec.cpg.graph.statements.ReturnStatement
import de.fraunhofer.aisec.cpg.graph.statements.expressions.AssignExpression
import de.fraunhofer.aisec.cpg.graph.statements.expressions.DeclaredReferenceExpression
import de.fraunhofer.aisec.cpg.graph.statements.expressions.Expression
import de.fraunhofer.aisec.cpg.helpers.SubgraphWalker
import de.fraunhofer.aisec.cpg.passes.GoExtraPass
import de.fraunhofer.aisec.cpg.passes.TranslationResultPass
import de.fraunhofer.aisec.cpg.passes.order.DependsOn
import io.clouditor.graph.nodes.labels.*
import io.clouditor.graph.plusAssign
import java.util.function.Consumer
import java.util.function.Predicate
import java.util.stream.Collectors

@DependsOn(GoExtraPass::class)
@DependsOn(DFGExtensionPass::class)
class LabelExtractionPass(ctx: TranslationContext) : TranslationResultPass(ctx) {

    private val predicatesToHandle: MutableMap<Predicate<Node>, Consumer<Node>> = mutableMapOf()

    private var edgesCachePass: BidirectionalEdgesCachePass = BidirectionalEdgesCachePass(ctx)

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

        // Labels that are equal to each other are merged to reduce them in size and and allow
        // associating them to Anonymization label
        mergeEqualLabels(t)
        // Connects labels and anonymization labels such that anonymization of labels can be
        // excluded from searches of sensitive dataflows
        connectAnonymizerToLabel(t)
    }

    fun mergeEqualLabels(t: TranslationResult) {
        val uniqueLabels: MutableSet<Label> = mutableSetOf()

        t.additionalNodes.filterIsInstance<Label>().forEach {
            val label = it
            val mergeableLabels: Set<Label> =
                uniqueLabels.filter { ul -> ul.areMergeable(it) }.toSet()
            when (mergeableLabels.size) {
                0 -> uniqueLabels.add(label)
                1 -> mergeableLabels.first().mergeWith(label)
                else -> log.error("Uniqueness of mergeable label list seems to be violated")
            }
        }

        t.additionalNodes.removeIf { it is Label && !uniqueLabels.contains(it) }
    }

    fun connectAnonymizerToLabel(t: TranslationResult) {
        val anonLabels: Set<AnonLabel> = t.additionalNodes.filterIsInstance<AnonLabel>().toSet()
        val labels: Set<Label> =
            t.additionalNodes.filterIsInstance<Label>().filter { !anonLabels.contains(it) }.toSet()

        labels.forEach {
            val privLabel = it
            anonLabels.filter { it.canAnonymize(privLabel) }.map { it.addAnonymize(privLabel) }
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
            when (it.name.localName) {
                "PrivacyLabel" -> label = handlePrivacyLabelAnnotation(annotationParent, it)
                "Identifier" -> label = labelCreationDispatcher<Identifier>(annotationParent)
                "PseudoIdentifier" ->
                    label = labelCreationDispatcher<PseudoIdentifier>(annotationParent)
                "AnonPrivacyLabel" -> label = handleAnonPrivacyLabelAnnotation(annotationParent, it)
                "AnonIdentifier" ->
                    label = anonLabelCreationDispatcher<Identifier>(annotationParent)
                "AnonPseudoIdentifier" ->
                    label = anonLabelCreationDispatcher<PseudoIdentifier>(annotationParent)
            }
            label?.let { t += it } // Adding Labels to the supplementary nodes of a translation unit
        }
    }

    inline fun <reified T : Label> anonLabelCreationDispatcher(node: Node): AnonLabel {
        val anonLabel = labelCreationDispatcher<AnonLabel>(node)
        val anonymizedDummyLabel = initLabel<T>(node)
        anonymizedDummyLabel.labeledNodes.clear() // To mark it as Dummy

        anonLabel.anonymizes = anonymizedDummyLabel
        return anonLabel
    }

    /**
     * Extracts labels from Annotations of name "PrivacyLabel" including the attribute of a privacy
     * level. Edges are attached to the DFG-Border nodes. Nodes that are in the Sub-AST of the
     * annotated node, and have an outgoing DFG-edge to another node not in the annotated nodes
     * Sub-AST
     */
    private fun handleComment(t: TranslationResult, nodeWComment: Node) {
        val regexes =
            mutableMapOf(
                Regex("@Identifier($|\\s)") to
                    { node, _ ->
                        listOf(labelCreationDispatcher<Identifier>(node))
                    },
                Regex("@PseudoIdentifier(\$|\\s)") to
                    { node, _ ->
                        listOf(labelCreationDispatcher<PseudoIdentifier>(node))
                    },
                Regex("@PrivacyLabel\\(level=([0-9]+)\\)($|\\s)") to
                    this::handlePrivacyLabelComments,
                Regex("@AnonIdentifier($|\\s)") to
                    { node, _ ->
                        anonCommentLabelCreationDispatcher<Identifier>(node)
                    },
                Regex("@AnonPseudoIdentifier(\$|\\s)") to
                    { node, _ ->
                        anonCommentLabelCreationDispatcher<PseudoIdentifier>(node)
                    },
                Regex("@AnonPrivacyLabel\\(level=([0-9]+)\\)($|\\s)") to
                    this::handleAnonPrivacyLabelComments,
            )

        regexes.entries.forEach { it ->
            val matches = it.key.findAll(nodeWComment.comment!!)
            if (matches.toList().isNotEmpty()) {
                val labels = it.value(nodeWComment, matches)
                labels.forEach {
                    t += it // Adding Labels to the supplementary nodes of a translation unit
                }
            }
        }
    }

    private inline fun <reified T : Label> anonCommentLabelCreationDispatcher(
        node: Node
    ): List<AnonLabel> {
        val anonLabel = labelCreationDispatcher<AnonLabel>(node)
        val anonymizedDummyLabel = initLabel<T>(node)
        anonymizedDummyLabel.labeledNodes.clear() // To mark it as Dummy

        anonLabel.anonymizes = anonymizedDummyLabel
        return listOf(anonLabel)
    }

    private fun handleAnonPrivacyLabelComments(
        node: Node,
        matches: Sequence<MatchResult>
    ): List<Label> {
        val labels: MutableList<Label> = mutableListOf()
        matches.forEach {
            val thisLevel: Int? = it.destructured.toList().first().toIntOrNull()
            thisLevel?.let {
                val label: AnonLabel = labelCreationDispatcher<AnonLabel>(node)
                val anonymizedDummyLabel = initLabel<PrivacyLabel>(node)
                anonymizedDummyLabel.labeledNodes.clear() // To mark it as Dummy

                anonymizedDummyLabel.protectionlevel = it
                label.anonymizes = anonymizedDummyLabel

                labels.add(label)
            }
        }
        return labels
    }

    private fun handlePrivacyLabelComments(
        node: Node,
        matches: Sequence<MatchResult>
    ): List<Label> {
        val labels: MutableList<Label> = mutableListOf()
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
        val values: MutableList<Expression?> =
            annotation
                .members
                .filter { member -> member.name.localName == "level" }
                .mapNotNull { member -> member.value }
                .toMutableList()
        // This is to handle annotation that don't use named attributes, e.g. decorators in
        // TypeScript that are
        // more of meta calls to functions
        if (values.isEmpty() && annotation.members.isNotEmpty()) {
            annotation.members.getOrNull(0)?.value?.let { values.add(it) }
        }
        if (values.isNotEmpty()) {
            val param: Node? = values.getOrNull(0)
            param?.let {
                val label = labelCreationDispatcher<PrivacyLabel>(annotationParent)

                label.protectionlevel = it.code!!.toIntOrNull() ?: 0
                return label
            }
        }
        return null
    }

    private fun handleAnonPrivacyLabelAnnotation(
        annotationParent: Node,
        annotation: Annotation
    ): Label? {
        val values: MutableList<Expression?> =
            annotation
                .members
                .filter { member -> member.name.localName == "level" }
                .mapNotNull { member -> member.value }
                .toMutableList()
        // This is to handle annotation that don't use named attributes, e.g. decorators in
        // TypeScript that are
        // more of meta calls to functions
        if (values.isEmpty() && annotation.members.isNotEmpty()) {
            annotation.members.getOrNull(0)?.value?.let { values.add(it) }
        }
        if (values.isNotEmpty()) {
            val param: Node? = values.getOrNull(0)
            param?.let {
                val label: AnonLabel = labelCreationDispatcher<AnonLabel>(annotationParent)
                val anonymizedDummyLabel = initLabel<PrivacyLabel>(annotationParent)
                anonymizedDummyLabel.labeledNodes.clear() // To mark it as Dummy

                anonymizedDummyLabel.protectionlevel = it.code!!.toIntOrNull() ?: 0
                label.anonymizes = anonymizedDummyLabel

                return label
            }
        }
        return null
    }

    inline fun <reified T : Label> labelCreationDispatcher(node: Node): T {
        val node = node
        val label: T = initLabel<T>(node)
        when (node) {
            is FunctionDeclaration -> {
                addLabelToDFGBorderEdges(node, label)
                // addLabelToReturnedExpressions(node,label)
                // Todo or to everything that is not a subcall to catch returns and writes to
                // pass
                // by reference objects
            }
            is RecordDeclaration -> {
                addLabelToDFGBorderEdges(node, label)
                addLabelToInstantiations(node, label)
            }
            is DeclarationStatement -> {
                // To connect to all border edges, we first need to iterate through all declared
                // variables and find their USAGEs
                val usages =
                    node.declarations.filterIsInstance<VariableDeclaration>().flatMap {
                        it.usageEdges
                    }
                usages.forEach { addLabelToDFGBorderEdges(it.end, label) }
            }
            is AssignExpression -> {
                val variableDeclarations =
                    node.lhs.filterIsInstance<DeclaredReferenceExpression>().map { it.refersTo }
                variableDeclarations.forEach { addLabelToDFGBorderEdges(it as Node, label) }
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
        val label: T = T::class.constructors.first().call(n)
        label.labeledNodes.add(n)
        return label
    }

    /** Adds a newly created Label to the DFG-Border nodes, */
    fun addLabelToDFGBorderEdges(n: Node, label: Label) {
        val dfgExitNodes: MutableList<Node> = getDFGPathEdges(n)!!.exits

        label.labeledNodes.addAll(dfgExitNodes)
    }

    /**
     * Labels are added to the dfg-edges that leave Nodes that create the given declaration. This is
     * such that we can add a label to all object creations of a sensitive Class.
     */
    fun addLabelToInstantiations(n: Node, label: Label) {
        if (n is Declaration) {
            edgesCachePass.getEdgeSourceOf(n, BidirectionalEdgesCachePass.EdgeLabel.INSTANTIATES)
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
    private fun getDFGPathEdges(n: Node?): SubgraphWalker.Border? {
        val border = SubgraphWalker.Border()
        val flattedASTTree = SubgraphWalker.flattenAST(n)
        val dfgNodes =
            flattedASTTree
                .stream()
                .filter { node: Node -> node.prevDFG.isNotEmpty() || node.nextDFG.isNotEmpty() }
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
