FROM maven:3.9.16-eclipse-temurin-21-alpine AS build
LABEL authors="exepulgar"

WORKDIR /workspace
COPY pom.xml .
COPY scrap_diputados_service/pom.xml ./scrap_diputados_service/pom.xml
COPY scrap_locations_service/pom.xml ./scrap_locations_service/pom.xml
COPY diputado_profile_service/pom.xml ./diputado_profile_service/pom.xml
COPY scrap_diputados_service/src ./scrap_diputados_service/src
RUN mvn clean package -pl scrap_diputados_service -am -q


FROM eclipse-temurin:21-jdk-alpine
WORKDIR /app
COPY --from=build /workspace/scrap_diputados_service/target/*.jar app.jar
EXPOSE 8081

ENTRYPOINT ["java", "-jar", "app.jar"]