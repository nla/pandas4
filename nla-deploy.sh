#!/bin/bash
mvn package
cp -v ui/target/*.jar gatherer/target/*.jar $1/
