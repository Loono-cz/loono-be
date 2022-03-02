FROM gradle:7.3.2-jdk17 as gradle

COPY . /home/gradle/backend
WORKDIR /home/gradle/backend

RUN gradle build --info --stacktrace --no-daemon -x test

FROM gcr.io/distroless/java17

COPY --from=gradle --chown=nonroot:nonroot /home/gradle/backend/backend/build/dists/loono-be.jar /app/loono-be.jar

WORKDIR "/app"
USER nonroot

ENTRYPOINT ["java","-Xmx256m","-jar","./loono-be.jar"]
