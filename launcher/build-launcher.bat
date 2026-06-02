@echo off
setlocal

set CSC=%WINDIR%\Microsoft.NET\Framework64\v4.0.30319\csc.exe
if not exist "%CSC%" (
  echo csc.exe not found.
  exit /b 1
)

"%CSC%" /target:winexe /optimize+ /nologo /reference:System.Windows.Forms.dll /reference:System.dll /out:"..\StartSatisfactory.exe" "StartSatisfactory.cs"
if errorlevel 1 exit /b 1

echo Launcher built: ..\StartSatisfactory.exe
