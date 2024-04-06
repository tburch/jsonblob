# Docker multi-stage build

# 1. Building the App with Maven
FROM gradle:7.6.4-jdk17-alpine

ADD . /
WORKDIR /

# Run Maven build
RUN ./gradlew clean build -x test

# We only need one runnable jar
RUN ls -l build/libs
RUN rm build/libs/jsonblob-*-runner.jar
RUN ls -l build/libs

# 2. Just using the build artifact and then removing the build-container
FROM openjdk:17-alpine

# Fix CVEs
RUN apk add apk-tools=2.12.6-r0
RUN apk add libcrypto1.1=1.1.1l-r0
RUN apk add libssl1.1=1.1.1l-r0
RUN apk add libtasn1=4.17.0-r1
RUN apk add zlib=1.2.12-r2

RUN apk update && apk upgrade

# Create a new user with UID 10014
RUN addgroup -g 10014 choreo && \
    adduser  --disabled-password  --no-create-home --uid 10014 --ingroup choreo choreouser

VOLUME /tmp

USER 10014

# Add Spring Boot app.jar to Container
COPY --from=0 "/build/libs/jsonblob-*-all.jar" app.jar

# Fire up our Spring Boot app by default
CMD [ "sh", "-c", "java $JAVA_OPTS -Djava.security.egd=file:/dev/./urandom -jar /app.jar" ]