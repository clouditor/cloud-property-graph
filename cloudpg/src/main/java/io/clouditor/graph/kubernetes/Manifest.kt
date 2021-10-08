package io.clouditor.graph.kubernetes

data class Manifest(
    var apiVersion: String,
    var kind: String,
    var metadata: Metadata?,
    var spec: Spec?
)

data class Metadata(
    var annotations: Any?,
    var name: String?,
    var namespace: String?,
    var labels: Any?
)

data class Spec(
    var rules: List<Rule>?,
    var ports: Any?,
    var selector: Any?,
    var replicas: Any?,
    var template: Any?
)

data class Rule(var host: String?, var http: Any?)
