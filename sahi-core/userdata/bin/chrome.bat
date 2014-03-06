@ECHO OFF
if "%1"=="" goto ERROR
if "%2"=="" goto ERROR
SET SAHI_HOME=..\..
SET SCRIPTS_PATH=scripts/%1
SET BROWSER=C:\Documents and Settings\%Username%\Local Settings\Application Data\Google\Chrome\Application\chrome.exe
SET BROWSER_PROCESS=chrome.exe
SET BROWSER_OPTION=--user-data-dir=$userDir\browser\chrome\profiles\sahi$threadNo --proxy-server=localhost:9999 --disable-popup-blocking 
SET START_URL=%2
SET THREADS=5
SET LOG_DIR=default
java -cp %SAHI_HOME%\lib\ant-sahi.jar net.sf.sahi.test.TestRunner %SCRIPTS_PATH% "%BROWSER%" %START_URL% %LOG_DIR% localhost 9999 %THREADS% %BROWSER_PROCESS% "%BROWSER_OPTION%"
goto :EOF

:ERROR
echo "Usage: %0 <sah file|suite file> <startURL>"
echo "File path is relative to userdata/scripts"
echo "Example:" 
echo "%0 demo/demo.suite http://sahi.co.in/demo/"
echo "%0 demo/sahi_demo.sah http://sahi.co.in/demo/"