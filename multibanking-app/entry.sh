#!/bin/sh
envsubst < /usr/share/nginx/html/assets/settings/settings.template.json > /usr/share/nginx/html/assets/settings/settings.json
nginx -g "daemon off;"
