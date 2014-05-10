if [ ! $SAHI_HOME ] 
then
	export SAHI_HOME=..
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
java -classpath $SAHI_CLASS_PATH net.sf.sahi.Proxy "$SAHI_HOME" "$SAHI_USERDATA_DIR_TMP"