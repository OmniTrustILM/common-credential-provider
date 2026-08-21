# Build stage
FROM maven:3.9.16-eclipse-temurin-21 AS build
COPY src /home/app/src
COPY pom.xml /home/app
COPY settings.xml /root/.m2/settings.xml
COPY docker /home/app/docker
RUN mvn -f /home/app/pom.xml clean package

# Package stage
FROM eclipse-temurin:21.0.12_8-jre-alpine

LABEL org.opencontainers.image.authors="ILM <support@otilm.com>"

# add non root user otilm
RUN addgroup --system --gid 10001 otilm && adduser --system --home /opt/otilm --uid 10001 --ingroup otilm otilm

# apk upgrade should be removed once CVEs will be fixed in eclipse-temurin:21-jdk-alpine-3.23
RUN apk update && \
  apk --no-cache upgrade && \
  apk add --no-cache curl

COPY --from=build /home/app/docker /
COPY --from=build /home/app/target/*.jar /opt/otilm/app.jar

WORKDIR /opt/otilm

ENV DB_SCHEMA=common_secret
ENV PORT=8080
ENV JAVA_OPTS=

USER 10001

ENTRYPOINT ["/opt/otilm/entry.sh"]
