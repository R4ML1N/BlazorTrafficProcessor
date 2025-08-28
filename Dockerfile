# ---- Build stage
FROM docker.io/library/gradle:8.9.0-jdk17 AS build
WORKDIR /app

# Copy Gradle wrapper & metadata first (better layer caching)
COPY gradlew gradlew.bat /app/
COPY gradle /app/gradle
COPY settings.gradle build.gradle /app/

# Then sources
COPY src /app/src

# Build fat JAR into /app/releases/
RUN gradle --no-daemon clean shadowJar

# ---- Export stage
FROM alpine:3.20
WORKDIR /out
COPY --from=build /app/releases/*.jar /out/