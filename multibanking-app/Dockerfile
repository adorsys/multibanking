FROM nginx:1.17-alpine

# support running as arbitrary user which belogs to the root group
RUN chmod 777 -R /var/cache/nginx /var/run /var/log/nginx

COPY default.conf /etc/nginx/conf.d/default.conf

COPY entry.sh /opt/entry.sh
RUN chmod 775 /opt/entry.sh

# comment user directive as master process is run as user in OpenShift anyhow
RUN sed -i.bak 's/^user/#user/' /etc/nginx/nginx.conf

WORKDIR /www/app

ADD . /www/app
RUN chmod 775 /www/app/platforms/browser/www/assets/settings/settings.json

RUN apk add --update nodejs nodejs-npm \
    && npm config set unsafe-perm true \
    && npm install -g ionic cordova \
    && npm rebuild node-sass \
    && npm install

RUN ./build.sh prod

EXPOSE 8081

ENTRYPOINT /opt/entry.sh
