#!/bin/bash

rm .env

hostIp=$(ifconfig | grep '\<inet\>' | cut -d ' ' -f2 | grep -v '127.0.0.1' | head -1)

echo "HOST_IP=$hostIp" >> .env

if [ $# -eq 1 ]; then
	if [ $1 = "debug" ]; then
		echo "KEYCLOAK_PARAMS=-b 0.0.0.0 --debug" >> .env
	fi
fi

cat .env

docker-compose up

rm .env
