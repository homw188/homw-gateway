@echo off
setlocal enabledelayedexpansion
cd /d %~dp0
for /f "delims=" %%i in ('type "config.ini"^| find /i "="') do set %%i
cd %platform%
nssm restart %serviceName%