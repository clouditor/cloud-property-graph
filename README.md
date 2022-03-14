# Cloud Property Graph (CloudPG)

[![build](https://github.com/clouditor/cloud-property-graph/actions/workflows/build.yml/badge.svg)](https://github.com/clouditor/cloud-property-graph/actions/workflows/build.yml)
![GitHub last commit](https://img.shields.io/github/last-commit/clouditor/cloud-property-graph)
![GitHub](https://img.shields.io/github/license/clouditor/cloud-property-graph)
[![](https://jitpack.io/v/clouditor/cloud-property-graph.svg)](https://jitpack.io/#clouditor/cloud-property-graph)


The Cloud Property Graph is based on a Code Property Graph and tries to connect static code analysis and Cloud runtime assessment. It is based on the [CPG](https://github.com/Fraunhofer-AISEC/cpg) project by Fraunhofer AISEC. We aim to contribute certain parts of this project back to the upstream repo, once they are more matured.

Furthermore, we plan to integrate a Go-based version of the CloudPG into our main Cloud assessment tool, [Clouditor](https://github.com/clouditor/clouditor).

This project primarily serves as a research sandbox and playground, so please do not expect API stability for now (or ever).

## Build

First, the graph classes need to be built from the Ontology definitions by calling `./build-ontology.sh`. We aim to automate this process using a Gradle plugin in the future. The build using `./gradlew installDist`.
## Usage
 
Start neo4j using `docker run -d --env NEO4J_AUTH=neo4j/password -p7474:7474 -p7687:7687 neo4j`. 

Run `cloudpg/build/install/cloudpg/bin/cloudpg`. This will print a help message with any additional needed parameters. The root path is required and the program can be called as follows: `cloudpg/build/install/cloudpg/bin/cloudpg --root=/x/testprogramm folder1/ folder2/ folder 3/`

## Developing Passes

Passes are extensions of the Cloud Property Graph that can create and modify nodes and edges.
Passes can be language- or framework-specific, for example to create a LogOutput node when a specific logging library is used.
The goal of passes is to abstract away language specifics and other details, allowing to write simple, generic, reusable queries

Some implementation details need to be considered when writing passes:
- New passes need to be registered in `App.kt` (`registerPass()` in the `doTranslate()` method)
- Passes must overwrite the `accept` and `cleanup` methods
- Newly created nodes need to be added to the `translationResult` (`t += n`)
- Add a DFG edge via `node1.addNextDFG(node2)`

## Testing
The CloudPG includes a testing library that provides test cases for certain threats. At the moment, these threats include primarily privacy threats. 
To use this testing library, a dedicated testing mode is available which can be enabled via the flag ```--local-mode```. For example, adapt the command above with the flags ```--local-mode --root=. ppg-testing-library/```. 

## Further reading

[1] Christian Banse, Immanuel Kunz, Angelika Schneider and Konrad Weiss. Cloud Property Graph: Connecting Cloud Security Assessments with Static Code Analysis. IEEE CLOUD 2021. https://doi.org/10.1109/CLOUD53861.2021.00014
