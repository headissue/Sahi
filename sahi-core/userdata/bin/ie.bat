@ECHO OFF
if "%1"=="" goto ERROR
if "%2"=="" goto ERROR
SET SAHI_HOME=..\..
SET SCRIPTS_PATH=scripts/%1
SET BROWSER=C:\Program Files\Internet Explorer\IEXPLORE.EXE
SET BROWSER_PROCESS=iexplore.exe
SET BROWSER_OPTION=-noframemerging
SET START_URL=%2
SET THREADS=6
SET LOG_DIR=default
%SAHI_HOME%\tools\backup_proxy_config.exe
%SAHI_HOME%\tools\proxy_config.exe sahi_https
java -cp %SAHI_HOME%\lib\ant-sahi.jar net.sf.sahi.test.TestRunner %SCRIPTS_PATH% "%BROWSER%" %START_URL% %LOG_DIR% localhost 9999 %THREADS% %BROWSER_PROCESS% "%BROWSER_OPTION%"
%SAHI_HOME%\tools\proxy_config.exe original
goto :EOF

:ERROR
echo "Usage: %0 <sah file|suite file> <startURL>"
echo "File path is relative to userdata/scripts"
echo "Example:" 
echo "%0 demo/demo.suite http://sahi.co.in/demo/"
echo "%0 demo/sahi_demo.sah http://sahi.co.in/demo/"