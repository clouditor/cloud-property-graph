#/bin/bash
pushd owl2java
./gradlew installDist
./build/install/owl2java/bin/owl2java
popd
