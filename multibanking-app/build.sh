#!/bin/sh
set -e
echo $1

ionic cordova platform remove browser && ionic cordova platform add browser
ionic cordova build browser --configuration=$1
