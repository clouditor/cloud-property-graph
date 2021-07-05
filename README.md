# Cloud Property Graph (CloudPG)

## Build

Build using `./gradlew installDist`

## Usage
 
Start neo4j using `docker run -d --env NEO4J_AUTH=neo4j/password -p7474:7474 -p7687:7687 neo4j` or `docker run -d --env NEO4J_AUTH=neo4j/password -p7474:7474 -p7687:7687 neo4j/neo4j-arm64-experimental:4.3.1-arm64` on ARM systems. 

Run `build/install/cloud-property-graph/bin/cloud-property-graph`. This will print a help message with any additional needed parameters.
