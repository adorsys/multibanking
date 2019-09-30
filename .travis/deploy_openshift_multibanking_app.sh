#!/usr/bin/env bash
set -e
docker login ${OPENSHIFT_REGISTRY} -u ${OPENSHIFT_USER} -p ${OPENSHIFT_TOKEN}
docker build -t ${OPENSHIFT_REGISTRY}/${OPENSHIFT_NAMESPACE}/multibanking-app:${TAG} --build-arg  build_env=${BUILD_ENV} multibanking-app/
docker push ${OPENSHIFT_REGISTRY}/${OPENSHIFT_NAMESPACE}/multibanking-app:${TAG}
