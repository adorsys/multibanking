#!/usr/bin/env bash
set -e

mvn --settings .travis/settings.xml clean package -B -V
