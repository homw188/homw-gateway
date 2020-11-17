@echo off
setlocal enabledelayedexpansion

rem 1.read config.ini
cd /d %~dp0
for /f "delims=" %%i in ('type "config.ini"^| find /i "="') do set %%i
set PATH=%cd%

rem 2.set path vars
set BASE_PATH=%PATH:~0,-5%
set BIN_PATH=%BASE_PATH%/bin
set LOG_PATH=%BASE_PATH%/logs

rem 3.make log dir
if not exist %LOG_PATH% md "%LOG_PATH%"

rem 4.execute
cd %platform%
nssm install %serviceName% %BIN_PATH%/run.bat
nssm set %serviceName% AppDirectory %BIN_PATH%
nssm set %serviceName% Description %description%
nssm set %serviceName% AppStdout %LOG_PATH%/%logOutName%
nssm set %serviceName% AppStderr %LOG_PATH%/%logErrName%
nssm set %serviceName% AppRotateFiles 1
nssm set %serviceName% AppRotateSeconds 86400
nssm set %serviceName% AppRotateBytes 1048576