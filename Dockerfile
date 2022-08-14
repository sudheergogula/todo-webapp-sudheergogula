FROM amazoncorretto:11-alpine-jdk
MAINTAINER Sudheer Gogula
COPY target/todowebapp-*.jar todowebapp.jar
ENTRYPOINT ["java", "-jar", "/todowebapp.jar"]