#!/bin/bash

mkdir -p logs
docker compose logs -t tracker 2>&1 | gzip > logs/tracker-"$(date +%Y-%m-%d_%H:%m:%S)".log.gz
docker compose -f docker-compose.yml -f docker-compose.deploy.yml --env-file .env --env-file .env.prod up -d --build
