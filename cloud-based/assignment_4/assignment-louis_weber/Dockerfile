FROM openjdk:12
ARG JAR_FILE=build/libs/demo-0.0.1-SNAPSHOT.jar
COPY build/libs/demo-0.0.1-SNAPSHOT.jar app.jar
ENTRYPOINT ["java","-jar","/app.jar"]
