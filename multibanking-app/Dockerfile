FROM nginx:1.17-alpine

ARG build_env
RUN echo "$build_env"

# support running as arbitrary user which belogs to the root group
RUN chmod 777 -R /var/cache/nginx /var/run /var/log/nginx

COPY default.conf /etc/nginx/conf.d/default.conf

# comment user directive as master process is run as user in OpenShift anyhow
RUN sed -i.bak 's/^user/#user/' /etc/nginx/nginx.conf

WORKDIR /www/app

ADD . /www/app

RUN apk add --update nodejs nodejs-npm \
    && npm config set unsafe-perm true \
    && npm install -g ionic cordova \
    && npm rebuild node-sass \
    && npm install

RUN ./build.sh "$build_env"

RUN cp -r platforms/browser/www/* /usr/share/nginx/html

EXPOSE 8081
