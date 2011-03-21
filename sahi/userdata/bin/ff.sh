#!/bin/bash
if [ $# -ne 1 ] 
then
	echo "Usage: ff.sh <sah file|suite file>"
	echo "File path is relative to userdata/scripts"
	echo "Example:" 
	echo "ff.sh demo/demo.suite"
	echo "ff.sh demo/sahi_demo.sah"
else
	export SAHI_HOME=../..
	export SCRIPTS_PATH=scripts/$1
	export BROWSER=firefox
	export BROWSER_PROCESS=firefox
	export BROWSER_OPTION="-profile \$userDir/browser/ff/profiles/sahi\$threadNo -no-remote"
	export START_URL=http://sahi.co.in/demo/
	export THREADS=3
	export LOG_DIR=default
	java -cp $SAHI_HOME/lib/ant-sahi.jar net.sf.sahi.test.TestRunner $SCRIPTS_PATH "$BROWSER" $START_URL $LOG_DIR localhost 9999 $THREADS $BROWSER_PROCESS "$BROWSER_OPTION"
fi