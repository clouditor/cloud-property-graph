package io.clouditor.graph

import java.nio.file.Path
import org.neo4j.ogm.config.Configuration
import org.neo4j.ogm.model.Result
import org.neo4j.ogm.session.SessionFactory

val configuration =
    Configuration.Builder().uri("bolt://localhost").credentials("neo4j", App.neo4jPassword).build()

val sessionFactory =
    SessionFactory(configuration, "de.fraunhofer.aisec.cpg.graph", "io.clouditor.graph")
val session = sessionFactory.openSession()

fun executePPG(rootPath: Path, subPaths: List<Path>, query: String): Result {
    session.purgeDatabase()
    App.let {
        App.rootPath = rootPath
        App.paths = subPaths
        App.labelsEnabled = true
        App.localMode = true
    }
    App.call()

    val result = session.query(query, emptyMap<String, String>())
    return result
}
