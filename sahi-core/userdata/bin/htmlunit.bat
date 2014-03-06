@ECHO OFF
if "%1"=="" goto ERROR
if "%2"=="" goto ERROR
SET SAHI_HOME=..\..
SET SCRIPTS_PATH=scripts/%1
SET BROWSER=D:/Dev/sahi/sahi_htmlunit/bin/htmlunit.bat
SET BROWSER_PROCESS=java.exe
SET BROWSER_OPTION=
SET START_URL=%2
SET THREADS=20
SET LOG_DIR=default
java -cp %SAHI_HOME%\lib\ant-sahi.jar net.sf.sahi.test.TestRunner %SCRIPTS_PATH% "%BROWSER%" %START_URL% %LOG_DIR% localhost 9999 %THREADS% %BROWSER_PROCESS% "%BROWSER_OPTION%"
goto :EOF

:ERROR
echo "Usage: %0 <sah file|suite file> <startURL>"
echo "File path is relative to userdata/scripts"
echo "Example:" 
echo "%0 demo/demo.suite http://sahi.co.in/demo/"
echo "%0 demo/sahi_demo.sah http://sahi.co.in/demo/"