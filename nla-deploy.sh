#!/bin/bash
mvn package
cp -v ui/target/*.jar gatherer/target/*.jar delivery/target/*.jar nomination/target/*.jar $1/
