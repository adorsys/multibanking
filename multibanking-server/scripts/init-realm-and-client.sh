#!/bin/bash

sh keycloak-admin-cli.sh config credentials --server http://keycloak:8080/auth --realm master --user admin --password admin123

echo "create realm multibanking"
sh keycloak-admin-cli.sh create realms -s realm=multibanking -s enabled=true

echo "create client multibanking-client"
sh keycloak-admin-cli.sh create clients -r multibanking -s clientId=multibanking-client -s 'redirectUris=["*"]' -s 'webOrigins=["*"]' -f multibanking-client.json
MBCLIENTID=$(sh keycloak-admin-cli.sh get clients -r multibanking -q clientId=multibanking-client --fields id | jq -r '.[] | .id')

echo "create client multibanking-server"
sh keycloak-admin-cli.sh create clients -r multibanking -s clientId=multibanking-server -f multibanking-server.json

echo "create roles"
sh keycloak-admin-cli.sh create roles -r multibanking -s name=rules_admin

echo "add user-secret-claim-mapper"
sh keycloak-admin-cli.sh create -r multibanking clients/$MBCLIENTID/protocol-mappers/models -s protocol=openid-connect -s name="user secret" -s protocolMapper=user-secret-claim-mapper

echo "modify browser flow"
sh keycloak-admin-cli.sh create authentication/flows/browser/copy -r multibanking -s "newName=sts browser"
sh keycloak-admin-cli.sh create -r multibanking authentication/flows/sts%20browser%20forms/executions/execution -s provider=sts-username-password-form
deleteId=$(sh keycloak-admin-cli.sh get -r multibanking authentication/flows/sts%20browser%20forms/executions | jq -r '.[]  | select(.providerId == "auth-username-password-form") | .id')
sh keycloak-admin-cli.sh delete -r multibanking authentication/executions/$deleteId
sh keycloak-admin-cli.sh get -r multibanking authentication/flows/sts%20browser%20forms/executions | jq -r '.[]  | select(.providerId == "sts-username-password-form") | .requirement="REQUIRED"' > sts-username-password-form.json
sh keycloak-admin-cli.sh update -r multibanking authentication/flows/sts%20browser%20forms/executions -f sts-username-password-form.json
raiseId=$(sh keycloak-admin-cli.sh get -r multibanking authentication/flows/sts%20browser%20forms/executions | jq -r '.[]  | select(.providerId == "sts-username-password-form") | .id')
sh keycloak-admin-cli.sh create -r multibanking authentication/executions/$raiseId/raise-priority

echo "modify direct grant flow"
sh keycloak-admin-cli.sh create authentication/flows/direct%20grant/copy -r multibanking -s "newName=sts direct grant"
sh keycloak-admin-cli.sh create -r multibanking authentication/flows/sts%20direct%20grant/executions/execution -s provider=sts-direct-access-authenticator
deleteId=$(sh keycloak-admin-cli.sh get -r multibanking authentication/flows/sts%20direct%20grant/executions | jq -r '.[]  | select(.providerId == "direct-grant-validate-password") | .id')
sh keycloak-admin-cli.sh delete -r multibanking authentication/executions/$deleteId
sh keycloak-admin-cli.sh get -r multibanking authentication/flows/sts%20direct%20grant/executions | jq -r '.[]  | select(.providerId == "sts-direct-access-authenticator") | .requirement="REQUIRED"' > sts-direct-access-authenticator.json
sh keycloak-admin-cli.sh update -r multibanking authentication/flows/sts%20direct%20grant/executions -f sts-direct-access-authenticator.json
raiseId=$(sh keycloak-admin-cli.sh get -r multibanking authentication/flows/sts%20direct%20grant/executions | jq -r '.[]  | select(.providerId == "sts-direct-access-authenticator") | .id')
sh keycloak-admin-cli.sh create -r multibanking authentication/executions/$raiseId/raise-priority

sh keycloak-admin-cli.sh update realms/multibanking -s "browserFlow=sts browser" -s "directGrantFlow=sts direct grant"

echo MB-CLIENT-ID $MBCLIENTID

rm sts-username-password-form.json
rm sts-direct-access-authenticator.json
