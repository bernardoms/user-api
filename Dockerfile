FROM openjdk:11

WORKDIR app

RUN curl -O "http://download.newrelic.com/newrelic/java-agent/newrelic-agent/current/newrelic-java.zip" && \
unzip newrelic-java.zip -d .

ADD target/*.jar /app/user-microservice-api.jar

ENTRYPOINT [ "sh", "-c", "java -javaagent:/app/newrelic/newrelic.jar $JAVA_OPTS -jar user-microservice-api.jar" ]