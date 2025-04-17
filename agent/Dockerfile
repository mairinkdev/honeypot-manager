FROM eclipse-temurin:11-jdk-focal

WORKDIR /app

COPY pom.xml ./
RUN apt-get update && apt-get install -y maven
RUN mvn dependency:go-offline -B

COPY src ./src

RUN mvn package -DskipTests

EXPOSE 2222 8080 21 25 3306

ENTRYPOINT ["java", "-cp", "target/classes:target/dependency/*", "com.mairink.honeypot.HoneypotAgent"] 