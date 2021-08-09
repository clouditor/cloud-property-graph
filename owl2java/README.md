# owl2java

## Intoduction
owl2java generates Java and Go files based on an Ontology file in OWL/XML or RDF/XML file format. Currently, it is not possible to generate only java or go files.


## Usage
During development the parameters are optional. The default values are definded in `src/main/java/io/clouditor/graph/SemanticNodeGenerator.kt`.

The parameters are as follows:
- 1st parameter: Ontology Input File (Only OWL/XML and RDF/XML are supported)
- 2st parameter: Java package name
- 3nd parameter: Output path for generated Java files
- 4th parameter: Go package name
- 5th parameter: Output path for generated Go Files

```./gradlew run --args "<paramters>"```

Full example: 
```
./gradlew run --args "resources/urn_webprotege_ontology_e4316a28-d966-4499-bd93-6be721055117.owx io.clouditor.graph output/java/ voc output/go"
```

## TODO
- separate generation of Java and Go files
- add a Java cli (e.g., https://picocli.info/)