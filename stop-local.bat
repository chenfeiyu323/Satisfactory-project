@echo off
setlocal
set ROOT=%~dp0

docker compose -f "%ROOT%docker-compose.yml" down
