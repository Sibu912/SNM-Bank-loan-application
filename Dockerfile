# SNM Bank loan service (backend/) as a container, for hosting online (docs/DEPLOY.md).
# Build:  docker build -t snm-loan-service .
# It lives in the repo root so a host can build straight from GitHub.

# ---- 1. Build the jar with Maven ----
FROM maven:3-eclipse-temurin-21 AS build
WORKDIR /build
COPY backend/pom.xml .
RUN mvn -B -ntp dependency:go-offline
COPY backend/src ./src
# Tests need a PostgreSQL database; GitHub Actions runs them before anything is deployed
RUN mvn -B -ntp package -DskipTests && cp target/*.jar /build/loan-service.jar

# ---- 2. Run it on a small Java runtime ----
FROM eclipse-temurin:21-jre
WORKDIR /app
RUN useradd --system --uid 10001 snm
COPY --from=build /build/loan-service.jar /app/loan-service.jar
COPY backend/docker/start.sh /app/start.sh
RUN chmod 755 /app/start.sh
USER snm

# Settings for a free instance with 512 MB of memory and a slow CPU
ENV JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=65 -XX:+UseSerialGC -Xss512k -XX:TieredStopAtLevel=1 -XX:ReservedCodeCacheSize=48m -XX:MaxMetaspaceSize=160m"
ENV PORT=8080
EXPOSE 8080
CMD ["/app/start.sh"]
