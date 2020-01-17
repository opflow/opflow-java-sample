FROM openjdk:8-jdk-alpine

RUN mkdir /app
WORKDIR /app

RUN mkdir /app/config
RUN mkdir /app/logs
RUN chmod 777 /app/logs

COPY target/opflow-java-sample-client.jar /app/

COPY src/main/resources/client.properties /app/config/
COPY src/main/resources/log4j-amqp.properties /app/config/
COPY src/main/resources/log4j.properties /app/config/

COPY src/misc/docker-entrypoint.sh /app/
RUN chmod +x /app/docker-entrypoint.sh

USER 1000

EXPOSE 8888
EXPOSE 8989

ENTRYPOINT [ "/app/docker-entrypoint.sh" ]
