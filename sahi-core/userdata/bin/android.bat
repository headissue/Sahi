
@ECHO OFF
if "%1"=="" goto ERROR
if "%2"=="" goto ERROR
SET SAHI_HOME=..\..
SET SCRIPTS_PATH=scripts/%1
SET BROWSER=D:\Dev\android-sdk-windows\tools\adb.exe
SET BROWSER_PROCESS=adb.exe
SET BROWSER_OPTION= shell am start -a android.intent.action.VIEW -d 
SET START_URL=%2
SET THREADS=1
SET LOG_DIR=default
java -cp %SAHI_HOME%\lib\ant-sahi.jar net.sf.sahi.test.TestRunner -test %SCRIPTS_PATH% -browser "%BROWSER%" -baseURL %START_URL% -htmlLog true -host localhost -port 9999 -threads %THREADS% -browserProcessName %BROWSER_PROCESS% -browserOption "%BROWSER_OPTION%"
goto :EOF

:ERROR
echo "Usage: %0 <sah file|suite file> <startURL>"
echo "File path is relative to userdata/scripts"
echo "Example:" 
echo "%0 demo/demo.suite http://sahi.co.in/demo/"
echo "%0 demo/sahi_demo.sah http://sahi.co.in/demo/"