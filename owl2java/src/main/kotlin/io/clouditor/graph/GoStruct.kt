package io.clouditor.graph

class GoStruct(val name: String, val parentClass: String) {
    var objectProperties: List<Properties> = listOf()
    var dataProperties: List<Properties> = listOf()
    var resourceTypes: List<String> = listOf() // e.g., BlockStorageType = []string {"BlockStorage", "Storage", "Resource"}

    var packageName: String? = null
    var structDescription: String? = null
}