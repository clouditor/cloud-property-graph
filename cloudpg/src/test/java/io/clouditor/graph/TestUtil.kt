package io.clouditor.graph

import de.fraunhofer.aisec.cpg.TranslationResult
import java.nio.file.Path
import org.neo4j.ogm.config.Configuration
import org.neo4j.ogm.model.Result
import org.neo4j.ogm.session.SessionFactory

val configuration =
    Configuration.Builder().uri("bolt://localhost").credentials("neo4j", App.neo4jPassword).build()

val sessionFactory =
    SessionFactory(configuration, "de.fraunhofer.aisec.cpg.graph", "io.clouditor.graph")
val session = sessionFactory.openSession()

fun executePPGAndQuery(rootPath: Path, subPaths: List<Path>, query: String): Result {
    session.purgeDatabase()
    App.let {
        it.rootPath = rootPath
        it.paths = subPaths
        it.labelsEnabled = true
        it.localMode = true
    }
    App.call()

    val result = session.query(query, emptyMap<String, String>())
    return result
}

fun executePPG(rootPath: Path, subPaths: List<Path>): TranslationResult {
    session.purgeDatabase()
    App.let {
        it.rootPath = rootPath
        it.paths = subPaths
        it.labelsEnabled = true
        it.localMode = true
    }
    return App.doTranslate()
}

fun executePPGAndStoreInNeo4j(rootPath: Path, subPaths: List<Path>) {
    session.purgeDatabase()
    App.let {
        it.rootPath = rootPath
        it.paths = subPaths
        it.labelsEnabled = true
        it.localMode = true
    }
    App.call()
}
