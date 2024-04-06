# Docker multi-stage build

# 1. Building the App with Maven
FROM 7.6.4-jdk17-alpine

ADD . /
WORKDIR /

# Just echo so we can see, if everything is there :)
RUN ls -l

# Run Maven build
RUN ./gradlew clean build -x test

# 2. Just using the build artifact and then removing the build-container
FROM openjdk:17-alpine

# Create a new user with UID 10014
RUN addgroup -g 10014 choreo && \
    adduser  --disabled-password  --no-create-home --uid 10014 --ingroup choreo choreouser

VOLUME /tmp

USER 10014

# Add Spring Boot app.jar to Container
COPY --from=0 "/build/libs/jsonblob-*-all.jar" app.jar

# Fire up our Spring Boot app by default
CMD [ "sh", "-c", "java $JAVA_OPTS -Djava.security.egd=file:/dev/./urandom -jar /app.jar" ]