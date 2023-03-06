FROM openjdk:19
COPY out/artifacts/techpanther-search-engine/techpanther-search-engine.jar techpanther-search-engine.jar
EXPOSE 8090
ENTRYPOINT ["java","-jar","/techpanther-search-engine.jar"]
