
FROM nginx:1.13-alpine

ENV ENV=$env

WORKDIR /www/app

ADD . /www/app

RUN apk add --update nodejs nodejs-npm \ 
    && npm install -g ionic cordova \
    && npm rebuild node-sass \
    && ionic cordova build browser --prod \
    cp -r platforms/browser/www/* /usr/share/nginx/html 