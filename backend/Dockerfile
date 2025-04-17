FROM eclipse-temurin:17-jdk-focal

WORKDIR /app

COPY .mvn/ .mvn
COPY mvnw pom.xml ./
RUN ./mvnw dependency:go-offline -B

COPY src ./src

RUN ./mvnw package -DskipTests
EXPOSE 8080

ENTRYPOINT ["java", "-jar", "target/honeypot-manager-backend-0.0.1-SNAPSHOT.jar"] 