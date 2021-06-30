FROM adoptopenjdk/openjdk11:latest
RUN addgroup --gid 1001 spring && adduser --uid 1001 --gid 1001 spring
USER spring:spring
ARG JAR_FILE=./build/dists/loono-be.jar
COPY ${JAR_FILE} app.jar
ENTRYPOINT ["java","-jar","/app.jar"]
