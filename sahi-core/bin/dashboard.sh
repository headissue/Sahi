#!/bin/sh

if [ ! $SAHI_HOME ]; then
  export SAHI_HOME=..
fi

SAHI_CLASS_PATH=$SAHI_HOME/lib/sahi-jar-with-dependencies.jar
java -classpath $SAHI_EXT_CLASS_PATH:$SAHI_CLASS_PATH net.sf.sahi.ui.Dashboard "${SAHI_HOME}" "${1}"