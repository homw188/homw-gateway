@echo off

rem 1.set basic vars
set JAVA=%JAVA_HOME%/bin/java.exe
set BIN_PATH=%~dp0
set BASE_PATH="%BIN_PATH:~0,-5%"
set JAR_FILE=%BASE_PATH%/target/gateway.jar
set CONFIG_PATH=file:%BASE_PATH%/
set LOG_PATH=%BASE_PATH%/logs

rem 2.check java exists
if not exist "%JAVA%" echo Please set the JAVA_HOME variable in your environment, we need java(x64)! jdk8 or later is better! & exit /b 1

rem 3.set jvm params
set JAVA_OPT=-Dconfigpath=%CONFIG_PATH% -Dlogpath=%LOG_PATH% -Xms1024m -Xmx1024m -XX:MetaspaceSize=128m 
set JAVA_OPT=%JAVA_OPT% -XX:MaxMetaspaceSize=128m -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=%LOG_PATH%/gateway_heapdump.hprof

rem 4.execute
call "%JAVA%" %JAVA_OPT% -jar %JAR_FILE% %*