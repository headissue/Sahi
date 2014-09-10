#!/bin/sh

script=""

case "$1" in
  /*) script="$1";;
  *) script="$PWD/$1";;
esac

US=`dirname $0`;
US=`(cd $US; pwd)`;

java -cp $US/../../sahi-test-runner/target/sahi-test-runner-4.4-jar-with-dependencies.jar  \
net.sf.sahi.test.TestRunner \
-test $script -browserType phantomjs \
-baseURL http://sahi.example.com/_s_/dyn/Driver_initialized \
-threads 1 \
-consoleLog true
