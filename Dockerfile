FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
COPY web ./web
RUN mvn -q clean package -DskipTests

FROM tomcat:11.0-jdk17-temurin
RUN rm -rf /usr/local/tomcat/webapps/*
COPY --from=build /app/target/ROOT.war /usr/local/tomcat/webapps/ROOT.war
EXPOSE 8080
CMD ["catalina.sh", "run"]
