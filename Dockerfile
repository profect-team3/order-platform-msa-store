FROM gradle:8.8-jdk17 AS builder
WORKDIR /workspace

COPY gradlew gradlew.bat settings.gradle ./
COPY gradle ./gradle
COPY order-platform-msa-store ./order-platform-msa-store
COPY order-platform-msa-store/build.cloud.gradle ./order-platform-msa-store/build.gradle

RUN ./gradlew :order-platform-msa-store:build -x test

FROM eclipse-temurin:17-jre-jammy
WORKDIR /app

COPY --from=builder /workspace/order-platform-msa-store/build/libs/*.jar /app/application.jar

EXPOSE 8082
ENTRYPOINT ["java", "-Dspring.profiles.active=prod", "-jar", "/app/application.jar"]
