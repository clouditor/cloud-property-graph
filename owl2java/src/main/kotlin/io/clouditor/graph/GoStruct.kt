package io.clouditor.graph

class GoStruct(val name: String, val parentClass: String) {
    var objectProperties: List<Properties> = listOf()
    var dataProperties: List<Properties> = listOf()

    var packageName: String? = null
    var description: String? = null
}