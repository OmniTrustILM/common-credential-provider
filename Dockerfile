# Build stage
FROM maven:3.9.9-eclipse-temurin-21 as build
COPY src /home/app/src
COPY pom.xml /home/app
COPY settings.xml /root/.m2/settings.xml
COPY docker /home/app/docker
RUN mvn -f /home/app/pom.xml clean package

# Package stage
FROM eclipse-temurin:21-jre-alpine

MAINTAINER CZERTAINLY <support@czertainly.com>

# add non root user czertainly
RUN addgroup --system --gid 10001 czertainly && adduser --system --home /opt/czertainly --uid 10001 --ingroup czertainly czertainly

COPY --from=build /home/app/docker /
COPY --from=build /home/app/target/*.jar /opt/czertainly/app.jar

WORKDIR /opt/czertainly

ENV PORT=8080
ENV JAVA_OPTS=

USER 10001

ENTRYPOINT ["/opt/czertainly/entry.sh"]
