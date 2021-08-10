package io.clouditor.graph.nodes

import io.clouditor.graph.DatabaseService
import io.clouditor.graph.DatabaseStorage

fun DatabaseService.getStorageOrCreate(name: String): DatabaseStorage {
    var storage = this.databaseStorages.firstOrNull() { it.name == name }

    if (storage == null) {
        storage = DatabaseStorage(mutableListOf(), null, this.geoLocation, mutableMapOf())
        storage.name = name
        this.databaseStorages.add(storage)
    }

    return storage
}
