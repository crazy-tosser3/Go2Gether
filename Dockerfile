FROM eclipse-temurin:25-jre-alpine

WORKDIR /app

# Jar собирается локально (./mvnw package, фронт уже внутри) и кладётся рядом с Dockerfile как app.jar
COPY app.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-XX:MaxRAMPercentage=60", "-jar", "app.jar"]
