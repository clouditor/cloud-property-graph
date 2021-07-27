# Cloud Property Graph (CloudPG)

[![build](https://github.com/clouditor/cloud-property-graph/actions/workflows/build.yml/badge.svg)](https://github.com/clouditor/cloud-property-graph/actions/workflows/build.yml)
![GitHub last commit](https://img.shields.io/github/last-commit/clouditor/cloud-property-graph)
![GitHub](https://img.shields.io/github/license/clouditor/cloud-property-graph)
[![](https://jitpack.io/v/clouditor/cloud-property-graph.svg)](https://jitpack.io/#clouditor/cloud-property-graph)


The Cloud Property Graph is based on a Code Property Graph and tries to connect static code analysis and Cloud runtime assessment. It is based on the [CPG](https://github.com/Fraunhofer-AISEC/cpg) project by Fraunhofer AISEC. We aim to contribute certain parts of this project back to the upstream repo, once they are more matured.

Furthermore, we plan to integrate a Go-based version of the CloudPG into our main Cloud assessment tool, [Clouditor](https://github.com/clouditor/clouditor).

This project primarily serves as a research sandbox and playground, so please do not expect API stability for now (or ever).

## Prerequisites
- Under Linux JAVA_HOME must be set
- Install jep, follow the instructions at https://github.com/Fraunhofer-AISEC/cpg#python
- Usage of experimental language, e.g., go
    - Checkout [Fraunhofer AISEC - Code Property Graph](https://github.com/Fraunhofer-AISEC/cpg) and build by using the property `-Pexperimental`:  `./gradlew build -Pexperimental`
    - The libcpgo.so must be placed somewhere in the `java.library.path`. (For further informaton see https://github.com/Fraunhofer-AISEC/cpg#usage-of-experimental-languages)
        - Under Linux in `/lib/`. `sudo cp ./cpg-library/src/main/golang/libcpgo.so /lib/` 
        - Und Mac in `~/Library/Java/Extensions`.

## Build

First, the graph classes need to be built from the Ontology definitions by calling `./build-ontology.sh`. We aim to automate this process using a Gradle plugin in the future. The build using `./gradlew installDist`.
## Usage
 
Start neo4j using `docker run -d --env NEO4J_AUTH=neo4j/password -p7474:7474 -p7687:7687 neo4j` or `docker run -d --env NEO4J_AUTH=neo4j/password -p7474:7474 -p7687:7687 neo4j/neo4j-arm64-experimental:4.3.2-arm64` on ARM systems. 

Run `cloudpg/build/install/cloudpg/bin/cloudpg`. This will print a help message with any additional needed parameters. The root path is required and the program can be called as follows: `cloudpg/build/install/cloudpg/bin/cloudpg --root=/x/testprogramm folder1/ folder2/ folder 3/`

## Further reading

We will share more details soon.

[1] Christian Banse, Immanuel Kunz, Angelika Schneider and Konrad Weiss. Cloud Property Graph: Connecting Cloud Security Assessments with Static Code Analysis. To appear in: IEEE CLOUD 2021 https://conferences.computer.org/cloud/2021/program/
