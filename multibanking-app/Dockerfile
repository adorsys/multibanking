FROM node:12-alpine as builder

WORKDIR /www/app

COPY package*.json ./

RUN npm install -g ionic cordova \
    && npm ci

COPY . .

RUN ./build.sh prod

# Stage 2
FROM nginx:1.17-alpine

# support running as arbitrary user which belogs to the root group
RUN chmod 777 -R /var/cache/nginx /var/run /var/log/nginx

COPY default.conf /etc/nginx/conf.d/default.conf

COPY entry.sh /opt/entry.sh
RUN chmod 775 /opt/entry.sh

# comment user directive as master process is run as user in OpenShift anyhow
RUN sed -i.bak 's/^user/#user/' /etc/nginx/nginx.conf

# remove default nginx html
RUN rm -rf /usr/share/nginx/html/*

# copy artificates from builder to default nginx directory
COPY --from=builder /www/app/platforms/browser/www /usr/share/nginx/html

RUN chmod 775 /usr/share/nginx/html/assets/settings/settings.json

EXPOSE 8081

ENTRYPOINT /opt/entry.sh
