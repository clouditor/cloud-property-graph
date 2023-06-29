package io.clouditor.graph.nodes

import de.fraunhofer.aisec.cpg.graph.Name
import io.clouditor.graph.DatabaseService
import io.clouditor.graph.DatabaseStorage

fun DatabaseService.getStorageOrCreate(name: String, parentName: String? = null): DatabaseStorage {
    var storage =
        this.storage.filterIsInstance<DatabaseStorage>().firstOrNull { it.name.localName == name }

    if (storage == null) {
        storage = DatabaseStorage(mutableListOf(), null, listOf(), this.geoLocation, mutableMapOf())
        storage.name = Name(name)

        // if the parent name was specified, try to look it up and set the parent(s)
        // TODO: why exactly is parents a list? FIX in the ontology?
        if (parentName != null) {
            storage.parent = this.storage.filter { it.name.localName == parentName }
        }

        this.storage.add(storage)
    }

    return storage
}
