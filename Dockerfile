FROM maven:3.8.6-eclipse-temurin-17 as build

WORKDIR /app

COPY pom.xml .
RUN mvn dependency:go-offline -B
COPY src ./src
RUN mvn clean package -DskipTests

FROM openjdk:17-slim

RUN groupadd -r appuser && useradd -r -g appuser appuser

WORKDIR /app

COPY --from=build /app/target/*.jar app.jar

COPY --from=build /app/src/main/resources/db/migration /app/db/migration

EXPOSE 8081

ENTRYPOINT ["java", "-jar", "app.jar"]
