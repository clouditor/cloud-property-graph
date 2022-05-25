# owl2java

## Intoduction
owl2java generates Java and Go files based on an Ontology file in OWL/XML or RDF/XML file format. Currently, it is not possible to generate only java or go files.


## Usage
During development the parameters are optional. The default values are definded in `src/main/java/io/clouditor/graph/SemanticNodeGenerator.kt`.

The parameters are as follows:
    - 1st parameter: Additional Java default constructor (true/false)
    - 2st parameter: Ontology Input File (Only OWL/XML and RDF/XML are supported)
    - 3nd parameter: Java package name
    - 4nd parameter: Output path for generated Java files (optional, but the order must be respected)
    - 5th parameter: Go package name
    - 6th parameter: Output path for generated Go Files (optional, but the order must be respected)   

```./gradlew run --args "<paramters>"```

Full example: 
```
./gradlew run --args "true resources/urn_webprotege_ontology_e4316a28-d966-4499-bd93-6be721055117.owx io.clouditor.graph output/java/ voc output/go"
```

## TODO
- separate generation of Java and Go files
- add a Java cli (e.g., https://picocli.info/)