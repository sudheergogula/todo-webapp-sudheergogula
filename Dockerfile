FROM amazoncorretto:11-alpine-jdk
LABEL app="todowebapp"
COPY target/todowebapp-*.jar todowebapp.jar
ENTRYPOINT ["java", "-jar", "/todowebapp.jar"]