FROM eclipse-temurin:21-jre

COPY target/*.jar world-cup-usa-2026.jar

EXPOSE 8090

ENTRYPOINT ["java", "-jar", "world-cup-usa-2026.jar"]
