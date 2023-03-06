FROM openjdk:19
COPY out/artifact/techpanther-search-engine/panther-search-engine.jar panther-search-engine.jar
EXPOSE 8090
ENTRYPOINT ["java","-jar","/panther-search-engine.jar"]
