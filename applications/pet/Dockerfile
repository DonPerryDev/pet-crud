FROM amazoncorretto:21-alpine3.22

RUN apk add --no-cache curl

WORKDIR /app
COPY build/libs/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]