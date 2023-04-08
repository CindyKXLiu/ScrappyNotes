# Dockerfile
FROM openjdk:17
EXPOSE 8080
ARG JAR_FILE=build/libs/webservice-0.0.1-SNAPSHOT.jar
ADD ${JAR_FILE} webservice.jar
ENTRYPOINT ["java","-jar","/webservice.jar"]