FROM gradle:7.5-jdk17 AS build

WORKDIR /app
COPY gradlew .
COPY gradlew.bat .
COPY gradle/ gradle/
COPY settings.gradle build.gradle ./

RUN chmod +x gradlew

RUN ./gradlew dependencies || true
COPY src/ src/
RUN ./gradlew clean bootJar -x test

FROM openjdk:17-jdk-slim
WORKDIR /app
COPY --from=build /app/build/libs/*.jar app.jar

ENTRYPOINT ["java","-jar","/app/app.jar"]