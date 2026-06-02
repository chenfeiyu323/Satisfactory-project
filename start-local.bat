@echo off
setlocal
set ROOT=%~dp0

if not exist "%ROOT%.env" if exist "%ROOT%.env.example" copy /Y "%ROOT%.env.example" "%ROOT%.env" >nul
if not exist "%ROOT%shared" mkdir "%ROOT%shared"

docker compose -f "%ROOT%docker-compose.yml" up -d --build
if errorlevel 1 (
  echo Failed to start services.
  exit /b 1
)

start "" "http://localhost:4200"
echo Started. Frontend: http://localhost:4200
