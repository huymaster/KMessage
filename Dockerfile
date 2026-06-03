FROM eclipse-temurin:25-jdk-alpine AS build
WORKDIR /app

COPY gradlew .
COPY gradle gradle
COPY build.gradle.kts .
COPY settings.gradle.kts .
COPY gradle.properties .
COPY core/build.gradle.kts core/
COPY api/build.gradle.kts api/

RUN --mount=type=cache,target=/root/.gradle \
    ./gradlew :core:dependencies --no-daemon

COPY . .

RUN --mount=type=cache,target=/root/.gradle \
    ./gradlew :core:buildFatJar --no-daemon --info

FROM eclipse-temurin:25-jre-alpine
WORKDIR /app

RUN apk add --no-cache libc6-compat gcompat argon2-dev
RUN addgroup -S server && adduser -S server -G server
RUN mkdir -p /app/_sessions_ && chown -R server:server /app/_sessions_ && chmod -R 777 /app

COPY --from=build --chown=server:server /app/core/build/libs/*-all.jar app.jar

USER server

EXPOSE 8080
ENTRYPOINT ["java", \
            "-XX:+UseG1GC", \
            "-XX:+UseCompactObjectHeaders", \
            "-jar", "app.jar"]