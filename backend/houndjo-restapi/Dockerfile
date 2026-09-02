# Stage 1 (builder): compile and package the application with Maven.
FROM amazoncorretto:21-alpine3.16 AS builder

RUN apk add --no-cache maven

WORKDIR /app

COPY pom.xml ./
COPY houndjo-core/pom.xml houndjo-core/pom.xml
COPY houndjo-app/pom.xml houndjo-app/pom.xml
COPY houndjo-tests/pom.xml houndjo-tests/pom.xml

RUN mvn -B -q -DskipTests dependency:go-offline

COPY houndjo-core/src houndjo-core/src
COPY houndjo-app/src houndjo-app/src

RUN mvn -B -q -DskipTests -pl houndjo-app -am clean package

# Stage 2 (runtime): start from a clean image and copy only the executable JAR.
# We intentionally use FROM again for a multi-stage build to keep the final image smaller and safer. (No maven inside)
FROM amazoncorretto:21-alpine3.16

RUN apk add --no-cache tzdata wget

WORKDIR /opt/houndjo

COPY --from=builder /app/houndjo-app/target/*-exec.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/opt/houndjo/app.jar"]
