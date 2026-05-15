# ============================================================
# Dockerfile — LeBonPlan API
# Build multi-stage pour minimiser la taille de l'image
# ============================================================

# Stage 1 : Build
FROM eclipse-temurin:21-jdk-alpine AS builder
WORKDIR /app

COPY pom.xml .
COPY src ./src

RUN apk add --no-cache maven && \
    mvn clean package -DskipTests -q

# Stage 2 : Runtime
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Utilisateur non-root pour la sécurité
RUN addgroup -S lebonplan && adduser -S lebonplan -G lebonplan
USER lebonplan

COPY --from=builder /app/target/*.jar app.jar

# Port exposé (Railway / Render utilisent $PORT)
EXPOSE 8080

# JVM optimisée pour les containers (mémoire limitée sur free tier)
ENTRYPOINT ["java", \
  "-XX:+UseContainerSupport", \
  "-XX:MaxRAMPercentage=75.0", \
  "-Djava.security.egd=file:/dev/./urandom", \
  "-jar", "app.jar"]
