
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app


COPY pom.xml .
RUN mvn dependency:go-offline -B


COPY src ./src
RUN mvn clean package -DskipTests


FROM eclipse-temurin:21-jre-alpine
WORKDIR /app


RUN apk add --no-cache tini
COPY --from=build /app/target/*.jar app.jar

EXPOSE 8761
ENTRYPOINT ["/sbin/tini", "--", "java", "-jar", "app.jar"]