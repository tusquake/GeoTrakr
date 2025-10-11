# -------------------------------
# Stage 1: Build the JAR
# -------------------------------
FROM maven:3.9.5-eclipse-temurin-17 AS build
WORKDIR /app

# Copy Maven config and pre-fetch dependencies
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source and build the JAR
COPY src ./src
RUN mvn clean package -DskipTests

# -------------------------------
# Stage 2: Run the app
# -------------------------------
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Install wget (for Render health checks)
RUN apk add --no-cache wget

# Copy JAR from build stage
COPY --from=build /app/target/*.jar app.jar

# Expose app port
EXPOSE 8080

# Health check for Render
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:8080/actuator/health || exit 1

# JVM options
ENV JAVA_OPTS="-Xmx512m -Xms256m"

# Spring profile (Render will override this)
ENV SPRING_PROFILES_ACTIVE=production

# Run the app
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
