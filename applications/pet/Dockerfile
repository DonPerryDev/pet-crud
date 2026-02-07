FROM amazoncorretto:21-alpine3.22

WORKDIR /app

RUN apk add --no-cache curl && \
    addgroup -g 1001 appgroup && \
    adduser -D -s /bin/sh -u 1001 -G appgroup appuser

COPY build/libs/*.jar app.jar

RUN chown -R appuser:appgroup /app

USER appuser

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]