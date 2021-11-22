#!/bin/zsh
rm -rf ./target
mvn clean
mvn package
docker build --tag shin6949/iter:latest .