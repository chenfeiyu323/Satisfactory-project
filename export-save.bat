@echo off
setlocal
set ROOT=%~dp0

if not exist "%ROOT%shared" mkdir "%ROOT%shared"

curl.exe -fS "http://localhost:8080/api/admin/save/export" -o "%ROOT%shared\satisfactory-save.json"
if errorlevel 1 (
  echo Export failed.
  exit /b 1
)

echo Save exported to shared\satisfactory-save.json
