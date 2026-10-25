# --- Build mərhələsi ---
FROM gradle:8.14-jdk21 AS build
WORKDIR /app
# Əvvəlcə yalnız build fayllarını kopyala ki, asılılıqlar keşlənsin
COPY settings.gradle build.gradle ./
COPY gradle ./gradle
RUN gradle --no-daemon dependencies --quiet || true
COPY src ./src
RUN gradle --no-daemon bootJar --quiet

# --- İşləmə mərhələsi ---
FROM eclipse-temurin:21-jre AS runtime
WORKDIR /app

# Root olmayan istifadəçi
RUN groupadd --system armudu && useradd --system --gid armudu armudu

COPY --from=build /app/build/libs/*.jar app.jar
RUN mkdir -p /app/data && chown -R armudu:armudu /app
USER armudu

EXPOSE 8080
ENV JAVA_OPTS=""
HEALTHCHECK --interval=30s --timeout=3s --start-period=40s \
  CMD wget -qO- http://localhost:8080/api/menu >/dev/null 2>&1 || exit 1

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
