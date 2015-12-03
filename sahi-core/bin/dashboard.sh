#!/bin/sh
US=`dirname $0`;
US=`(cd $US; pwd)`;

if [ ! $SAHI_HOME ]; then
  export SAHI_HOME=$US/..
fi

if [ ! $SAHI_USERDATA_DIR ]
then
	export SAHI_USERDATA_DIR=$SAHI_HOME/userdata
fi

SAHI_CLASS_PATH=$SAHI_HOME/lib/sahi-jar-with-dependencies.jar
java -Dsun.net.http.allowRestrictedHeaders=true -classpath $SAHI_EXT_CLASS_PATH:$SAHI_CLASS_PATH net.sf.sahi.ui.Dashboard "${SAHI_HOME}" "${SAHI_USERDATA_DIR}"
