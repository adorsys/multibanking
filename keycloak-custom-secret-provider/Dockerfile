FROM jboss/keycloak-postgres:3.1.0.Final
MAINTAINER https://github.com/adorsys/multibanking/keycloak-custom-secret-provider

COPY ./target/keycloak-custom-secret-provider.jar /opt/jboss/keycloak/providers/keycloak-custom-secret-provider.jar

EXPOSE 8080
