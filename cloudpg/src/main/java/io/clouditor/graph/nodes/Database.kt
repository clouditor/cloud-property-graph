package io.clouditor.graph.nodes

import io.clouditor.graph.DatabaseService
import io.clouditor.graph.DatabaseStorage

fun DatabaseService.getStorageOrCreate(name: String, parentName: String? = null): DatabaseStorage {
    var storage = this.databaseStorages.firstOrNull() { it.name == name }

    if (storage == null) {
        storage = DatabaseStorage(mutableListOf(), null, this.geoLocation, mutableMapOf())
        storage.name = name

        // if the parent name was specified, try to look it up and set the parent(s)
        // TODO: why exactly is parents a list? FIX in the ontology?
        if (parentName != null) {
            storage.parent = this.databaseStorages.filter { it.name == parentName }
        }

        this.databaseStorages.add(storage)
    }

    return storage
}
