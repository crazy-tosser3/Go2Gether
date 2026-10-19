FROM eclipse-temurin:25-jre-alpine

WORKDIR /app

COPY STOLOTO-1.0.0-RELEASE.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]