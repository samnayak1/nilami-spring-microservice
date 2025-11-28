# Build stage
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline -B
# download all necessary dependencies into the local repository before copying the source code. This is a common optimization technique: if only the source code changes, Docker can use the cached layer for dependencies, speeding up subsequent builds.
COPY src ./src

#packages the application into an executable JAR file
RUN mvn clean package 

# Runtime stage
FROM eclipse-temurin:21-jdk
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8085
ENTRYPOINT ["java", "-jar", "app.jar"]

