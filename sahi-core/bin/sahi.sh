#!/bin/sh
US=`dirname $0`;
US=`(cd $US; pwd)`;

if [ ! $SAHI_HOME ]
then
	# working directory $SAHI_HOME/bin expected
	export SAHI_HOME=$US/..
fi
if [ ! $SAHI_USERDATA_DIR ]
then
	export SAHI_USERDATA_DIR=$SAHI_HOME/userdata
fi

echo --------
echo SAHI_HOME: $SAHI_HOME
echo SAHI_USERDATA_DIR: $SAHI_USERDATA_DIR
echo --------

SAHI_CLASS_PATH=$SAHI_HOME/lib/sahi-jar-with-dependencies.jar
java -classpath $SAHI_CLASS_PATH net.sf.sahi.Proxy "${SAHI_HOME}" "${SAHI_USERDATA_DIR}"
