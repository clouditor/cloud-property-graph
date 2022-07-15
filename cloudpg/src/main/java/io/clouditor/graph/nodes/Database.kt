package io.clouditor.graph.nodes

import io.clouditor.graph.DatabaseService
import io.clouditor.graph.DatabaseStorage

fun DatabaseService.getStorageOrCreate(name: String, parentName: String? = null): DatabaseStorage {
    var storage = this.storages.filterIsInstance<DatabaseStorage>().firstOrNull { it.name == name }

    if (storage == null) {
        storage = DatabaseStorage(mutableListOf(), null, listOf(), this.geoLocation, mutableMapOf())
        storage.name = name

        // if the parent name was specified, try to look it up and set the parent(s)
        // TODO: why exactly is parents a list? FIX in the ontology?
        if (parentName != null) {
            storage.parent = this.storages.filter { it.name == parentName }
        }

        this.storages.add(storage)
    }

    return storage
}
