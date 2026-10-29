# Image to run NexusMarket inside Docker, following the reference project.
# docker-compose.yml (in the NexusMarket folder) builds it and connects it to MySQL and MongoDB.
FROM maven:3.9-eclipse-temurin-17

WORKDIR /workspace

COPY pom.xml ./pom.xml
COPY src ./src

EXPOSE 8080

CMD ["mvn", "-B", "spring-boot:run"]