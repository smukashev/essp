#!/bin/sh

mkdir -p ./src/main
mkdir -p ./src/main/resources

if ! [ -L ./src/main/java ];
then
    ln -s ./../../docroot/WEB-INF/src ./src/main/java
fi

if ! [ -L ./src/main/webapp ];
then
    ln -s ./../../docroot/ ./src/main/webapp
fi