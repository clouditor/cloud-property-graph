package io.clouditor.graph.testing

data class TestConfig(var services: List<Service>)

data class Service(
    var type: String,
    var directory: String,
    var name: String,
    var host: String?,
    var storages: List<String>?
)
