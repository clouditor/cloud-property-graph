package io.clouditor.graph.docker

data class DockerCompose(var version: String, var services: Map<String, Service>)

data class Service(var image: String?, var ports: List<String>)
