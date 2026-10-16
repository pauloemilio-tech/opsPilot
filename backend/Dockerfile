FROM eclipse-temurin:17-jdk-alpine AS build

WORKDIR /workspace

COPY .mvn .mvn
COPY mvnw pom.xml ./
RUN sed -i 's/\r$//' mvnw \
    && chmod +x mvnw \
    && ./mvnw -B -ntp dependency:go-offline

COPY src src
RUN ./mvnw -B -ntp clean package -DskipTests \
    && cp target/opspilot-backend-*.jar app.jar

FROM eclipse-temurin:17-jre-alpine

RUN addgroup -S opspilot \
    && adduser -S -G opspilot opspilot

WORKDIR /app
COPY --from=build --chown=opspilot:opspilot /workspace/app.jar app.jar

USER opspilot
EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
