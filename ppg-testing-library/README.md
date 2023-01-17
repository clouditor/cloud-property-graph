# Cloud Property Graph / Privacy Property Graph Testing Library

Before using the testing library, make sure to follow the setup steps described in the [main README](https://github.com/clouditor/cloud-property-graph/blob/main/README.md). 

The testing library consists of two parts: 1) the actual code to be tested which implements various privacy threats and 2) the JUnit tests that execute the Privacy Property Graph on the code (which can be found [here](https://github.com/clouditor/cloud-property-graph/tree/main/cloudpg/src/test/java/io/clouditor/graph)).
It is divided into the LINDDUN categories Linkability, Identifiability, Non-Repudiation, Detectability, Disclosure, Unawareness, Policy Non-Compliance. For more information about LINDDUN, see the [linddun website](https://www.linddun.org/).

In its current state, the testing library implements 22 tests, each for Python and Go. 
It also includes validation test cases which test for the *non*-existence of a threat. This way, we validate the correct implementation of the corresponding test case. 

## Usage
To execute tests, we recommend executing the existing JUnit tests. A specific test can simply be executed using gradle: `gradle test --tests io.clouditor.graph.NonRepudiationTest.testNR2PythonLoggingValidation`.
Note that the tests use a dedicated mode called ``local-mode``, which enables a specific pass that scans for config files and creates mock hosts and databases according to its specification. It also disables some passes that are designed to retrieve information from the cloud. This mode can also manually be enabled via a flag: `cloudpg/build/install/cloudpg/bin/cloudpg --enable-labels --local-mode --root=/x/testprogramm folder1/`

## Reproduce Test Results With Docker
`docker build -t "ppg" .`
`docker run -d --name ppg-tests ppg`
`docker exec ppg-tests ./gradlew test`