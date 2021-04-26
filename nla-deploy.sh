#!/bin/bash
mvn package
cp -v ui/target/*.jar gatherer/target/*.jar delivery/target/*.jar $1/
