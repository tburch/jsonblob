# Docker multi-stage build

# 1. Building the App with Maven
FROM gradle:7.6.4-jdk17-alpine as builder

ADD . /
WORKDIR /

# Run Maven build
RUN ./gradlew clean build -x test

# 2. Just using the build artifact and then removing the build-container
FROM gcr.io/distroless/java17-debian12:nonroot

# Create a new user with UID 10014
#RUN addgroup -g 10014 choreo && \
#    adduser  --disabled-password  --no-create-home --uid 10014 --ingroup choreo choreouser

USER 10014

EXPOSE 80

# Add jar to Container
COPY --from=builder /build/libs /

# Fire up our Spring Boot app by default
CMD ["/jsonblob-1.0.2-all.jar" ]