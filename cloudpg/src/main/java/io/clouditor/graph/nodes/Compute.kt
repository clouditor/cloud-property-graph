package io.clouditor.graph.nodes

import io.clouditor.graph.Container

fun Container.isInSelector(selector: Map<String, String>): Boolean {
    for (entry in selector.entries) {
        if (!labels.containsKey(entry.key) || labels[entry.key] != entry.value) {
            // fail fast
            return false
        }
    }

    return true
}
