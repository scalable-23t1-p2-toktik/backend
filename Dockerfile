FROM maven:3.8.4 as build

WORKDIR /app

COPY video-upload/pom.xml .
COPY video-upload/src ./src


RUN mvn clean package


FROM openjdk:17


WORKDIR /app


COPY --from=build /app/target/video-upload-0.0.1-SNAPSHOT.jar ./app.jar


EXPOSE 8080


CMD ["java", "-jar", "app.jar"]
