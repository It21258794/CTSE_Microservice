FROM openjdk:17-jdk-slim
WORKDIR /app
COPY build/libs/authservice-0.0.1-SNAPSHOT.jar auth-service.jar
ENTRYPOINT ["java", "-jar", "auth-service.jar"]
