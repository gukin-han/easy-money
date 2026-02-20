# Stage 1: Build frontend
FROM node:22-alpine AS frontend
WORKDIR /app/frontend
COPY frontend/package.json frontend/package-lock.json ./
RUN npm ci
COPY frontend/ ./
RUN npm run build

# Stage 2: Build backend
FROM eclipse-temurin:21-jdk AS backend
WORKDIR /app
COPY gradle/ gradle/
COPY gradlew build.gradle.kts settings.gradle.kts ./
COPY src/ src/
COPY --from=frontend /app/frontend/dist/ src/main/resources/static/
RUN ./gradlew bootJar -x test --no-daemon -Dorg.gradle.jvmargs="-Xmx1g"

# Stage 3: Runtime
FROM eclipse-temurin:21-jre
WORKDIR /app
RUN sed -i 's/jdk.tls.disabledAlgorithms=.*/jdk.tls.disabledAlgorithms=/' $JAVA_HOME/conf/security/java.security
COPY --from=backend /app/build/libs/easymoney-0.0.1-SNAPSHOT.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]
