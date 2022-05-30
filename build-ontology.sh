#!/bin/bash
pushd owl2java
./gradlew installDist 
# argument "true" enables creation of default constructors
./build/install/owl2java/bin/owl2java "true"
popd