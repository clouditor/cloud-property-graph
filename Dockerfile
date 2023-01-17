FROM neo4j:latest

ENV NEO4J_AUTH=neo4j/password

ADD . .

RUN ./build-ontology.sh
RUN ./gradlew installDist