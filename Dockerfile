FROM eclipse-temurin:17-jdk-alpine
ARG JAR_FILE=target/FinalTgSpringBoot-0.0.1-SNAPSHOT.jar
COPY ${JAR_FILE} application.jar
EXPOSE 8444
ENTRYPOINT ["java","-jar","/application.jar"]