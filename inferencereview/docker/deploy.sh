#!/usr/bin/env bash
cp ../target/inferencereview-0.0.1.war inferencereview.war
docker-compose build
docker-compose up