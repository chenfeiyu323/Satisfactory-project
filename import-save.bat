@echo off
setlocal
set ROOT=%~dp0
set FILE=%ROOT%shared\satisfactory-save.json

if not exist "%FILE%" (
  echo Save file not found: %FILE%
  exit /b 1
)

curl.exe -fS -X POST "http://localhost:8080/api/admin/save/import?overwrite=true" -F "file=@%FILE%"
if errorlevel 1 (
  echo Import failed.
  exit /b 1
)

echo Save imported.
