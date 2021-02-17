#!/bin/bash
mvn package
cp -v ui/target/*.jar $1/
