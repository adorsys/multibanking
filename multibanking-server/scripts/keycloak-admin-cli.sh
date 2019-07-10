#!/bin/sh

keycloakVersion=3.4.3.Final

if [ ! -f keycloak-admin-cli-$keycloakVersion.jar ]; then
    curl http://central.maven.org/maven2/org/keycloak/keycloak-admin-cli/$keycloakVersion/keycloak-admin-cli-$keycloakVersion.jar >> keycloak-admin-cli-$keycloakVersion.jar
fi

java -cp keycloak-admin-cli-$keycloakVersion.jar org.keycloak.client.admin.cli.KcAdmMain "$@"
