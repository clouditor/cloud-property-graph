#/bin/bash
pushd owl2java
./gradlew installDist
./build/install/owl2java/bin/OWL2JAVA
popd
