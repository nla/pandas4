#!/bin/bash
mvn package
cp -v ui/target/*.jar gatherer/target/*.jar delivery/target/*.jar "$1"/

if [ ! -z ${BUILD_FFMPEG+x} ]; then
  bash delivery/build-ffmpeg.sh
  cp -v build-ffmpeg/bin/ffmpeg "$1"
fi