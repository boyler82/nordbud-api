# --- Etap build: budujemy JAR Gradlem na JDK 21
FROM gradle:8.7-jdk21 AS build
WORKDIR /app
COPY . .
RUN gradle bootJar --no-daemon

# --- Etap run: lekki JRE 21
FROM eclipse-temurin:21-jre
WORKDIR /app
# Render przekazuje PORT w env; mamy fallback 8080 w application.properties
ENV JAVA_OPTS=""
COPY --from=build /app/build/libs/*.jar app.jar
EXPOSE 8080
CMD ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]