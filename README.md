# Cloud Property Graph (CloudPG)

The Cloud Property Graph is based on a Code Property Graph and tries to connect static code analysis and Cloud runtime assessment. It is based on the [CPG](https://github.com/Fraunhofer-AISEC/cpg) project by Fraunhofer AISEC. We aim to contribute certain parts of this project back to the upstream repo, once they are more matured.

Furthermore, we plan to integrate a Go-based version of the CloudPG into our main Cloud assessment tool, [Clouditor](https://github.com/clouditor/clouditor).

This project primarily serves as a research sandbox and playground, so please do not expect API stability for now (or ever).

## Build

Build using `./gradlew installDist`.

## Usage
 
Start neo4j using `docker run -d --env NEO4J_AUTH=neo4j/password -p7474:7474 -p7687:7687 neo4j` or `docker run -d --env NEO4J_AUTH=neo4j/password -p7474:7474 -p7687:7687 neo4j/neo4j-arm64-experimental:4.3.1-arm64` on ARM systems. 

Run `build/install/cloud-property-graph/bin/cloud-property-graph`. This will print a help message with any additional needed parameters.

## Further reading

We will share more details soon.
