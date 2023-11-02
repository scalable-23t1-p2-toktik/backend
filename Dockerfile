FROM maven:3.8.4 as build

WORKDIR /app

COPY video-upload/pom.xml .
COPY video-upload/src ./src


RUN mvn clean package


FROM openjdk:11


WORKDIR /app


COPY --from=build /app/target/video-upload-1.0.jar ./app.jar


EXPOSE 8080


CMD ["java", "-jar", "app.jar"]
