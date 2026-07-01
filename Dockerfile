FROM eclipse-temurin:21-jre
WORKDIR /app
COPY build/libs/*.jar app.jar

EXPOSE 8080

HEALTHCHECK --interval=15s --timeout=3s --retries=3 \
  CMD curl -f http://localhost:8080/actuator/health/readiness || exit 1

ENTRYPOINT ["java", "-XX:+UseZGC", "-XX:MaxRAMPercentage=40.0", "-XX:MaxMetaspaceSize=128m", "-Xss256k", "-jar", "app.jar"]
