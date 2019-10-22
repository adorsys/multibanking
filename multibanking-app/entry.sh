#!/bin/sh
envsubst < /www/app/platforms/browser/www/assets/settings/settings.template.json > /www/app/platforms/browser/www/assets/settings/settings.json
nginx -g "daemon off;"
