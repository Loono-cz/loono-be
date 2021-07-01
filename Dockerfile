FROM gcr.io/distroless/java:11
USER nonroot
ARG JAR_FILE=./build/dists/loono-be.jar
COPY ${JAR_FILE} app.jar
ENTRYPOINT ["java","-jar","/app.jar"]
