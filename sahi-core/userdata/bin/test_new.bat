@ECHO OFF
if "%1"=="" goto ERROR
if "%2"=="" goto ERROR
SET SAHI_HOME=..\..
SET SCRIPTS_PATH=scripts/%1
SET BROWSER=C:\Program Files\Mozilla Firefox\firefox.exe
SET BROWSER_PROCESS=firefox.exe
SET BROWSER_OPTION=-profile $userDir/browser/ff/profiles/sahi$threadNo -no-remote
SET START_URL=%2
SET THREADS=6
SET LOG_DIR=default
java -cp %SAHI_HOME%\lib\ant-sahi.jar net.sf.sahi.test.TestRunner -test %SCRIPTS_PATH% -browser "%BROWSER%" -baseURL %START_URL% -htmlLog true -host localhost -port 9999 -threads %THREADS% -browserProcessName %BROWSER_PROCESS% -browserOption "%BROWSER_OPTION%" -initJS "$user='test'; $pwd='secret'"
goto :EOF

:ERROR
echo "Usage: %0 <sah file|suite file> <startURL>"
echo "File path is relative to userdata/scripts"
echo "Example:" 
echo "%0 demo/demo.suite http://sahi.co.in/demo/"
echo "%0 demo/sahi_demo.sah http://sahi.co.in/demo/"