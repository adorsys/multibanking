FROM adorsys/openjdk-jre-base:8
MAINTAINER https://fgit.adorsys.de/adorsys/multibanking-mock
ENV JAVA_OPTS -Xmx512m
WORKDIR /opt/multibanking-mock
COPY ./target/multibanking-mock-*.jar .
EXPOSE 8080
CMD exec $JAVA_HOME/bin/java $JAVA_OPTS -jar /opt/multibanking-mock/multibanking-mock-*.jar
