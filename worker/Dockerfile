FROM openjdk:8-jdk-alpine

RUN mkdir /app
WORKDIR /app

RUN mkdir /app/config
RUN mkdir /app/logs
RUN chmod 777 /app/logs

ARG JAR_FILE_NAME

COPY target/${JAR_FILE_NAME} /app/

COPY src/main/resources/worker.properties /app/config/
COPY src/main/resources/log4j-file.properties /app/config/
COPY src/main/resources/log4j.properties /app/config/

COPY src/misc/docker-entrypoint.sh /app/
RUN chmod +x /app/docker-entrypoint.sh

USER 1000

EXPOSE 9450-9459

CMD [ "/app/docker-entrypoint.sh" ]
