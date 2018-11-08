#!/bin/bash

#JAVA_HOME="/usr/lib/jvm/java-1.7.0-openjdk-1.7.0.151.x86_64/jre"
#APP_HEAP_OPTS="-Xmx1G -Xms1G"
#APP_JVM_OPTS="-server -XX:+UseG1GC -XX:MaxGCPauseMillis=20 -XX:InitiatingHeapOccupancyPercent=35 -XX:+DisableExplicitGC -Djava.awt.headless=true"
APP_OPTS=""
###########################################################################
cd $(dirname $0)
APP_HOME=$(pwd)
APP_CLASS=com.fortis.test.CreateProto

LIB_JARS="$APP_HOME/conf"
for i in $APP_HOME/lib/*.jar; do
  LIB_JARS="$LIB_JARS":"$i"
done

if [ -z "$JAVA_HOME" ]; then
  JAVA="java"
else
  JAVA="$JAVA_HOME/bin/java"
fi


if [ -z "$APP_HEAP_OPTS" ]; then
    APP_HEAP_OPTS="-Xmx1G -Xms1G"
fi

if [ -z "$APP_JVM_OPTS" ]; then
  APP_JVM_OPTS="-server -XX:+UseG1GC -XX:MaxGCPauseMillis=20 -XX:InitiatingHeapOccupancyPercent=35 -XX:+DisableExplicitGC -Djava.awt.headless=true"
fi

CONSOLE="false"

while [ $# -gt 0 ]; do
    COMMAND=$1
    case $COMMAND in
        -c)
            CONSOLE="true"
            shift
            ;;
        *)
            ;;
    esac
done

PIDS=$(ps ax | grep -i "$APP_HOME" | grep java | grep -v grep | awk '{print $1}')
if [ ! -z "$PIDS" ]; then
    echo "$APP_HOME is running!"
    exit 1
fi


if [ ! -d "$APP_HOME/logs" ]; then
    mkdir "$APP_HOME/logs"
fi

COMMAND="$JAVA $APP_HEAP_OPTS $APP_JVM_OPTS $APP_OPTS -cp $LIB_JARS $APP_CLASS $@"
echo  $COMMAND
echo

if [ "x$CONSOLE" = "xfalse" ]; then
  nohup $COMMAND > "$APP_HOME/logs/stdout.log" 2>&1 < /dev/null &
else
  exec  $COMMAND
fi

echo "$APP_HOME is started "
PIDS=$(ps ax | grep -i "$APP_HOME" | grep java | grep -v grep | awk '{print $1}')
echo "PID: $PIDS"

