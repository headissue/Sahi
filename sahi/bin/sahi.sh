#if this does not work, try to run dos2unix on this file, or delete and reintroduce newlines.
SET EXT_CLASS_PATH=;
#SET EXT_CLASS_PATH=%EXT_CLASS_PATH%:..\extlib\mysql-connector-java-5.0.4-bin.jar
SET SAHI_CLASS_PATH=..\lib\sahi.jar
SET MOZ_NO_REMOTE=1
java -classpath %EXT_CLASS_PATH%;%SAHI_CLASS_PATH% net.sf.sahi.Proxy