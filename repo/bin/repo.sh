#!/bin/bash
#

if [ $# -le 0 ]; then
  echo "Usage: repo.sh (start|stop)"
  exit 1
fi

actionCmd=$1
shift

PRG="${0}"
BASEDIR=`dirname ${PRG}`
BIN_DIR=$BASEDIR

source ${BIN_DIR}/env.sh

if [ "$REPO_HOME" == "" ]; then
    REPO_HOME=`cd ${BIN_DIR}/..;pwd`
fi
echo REPO_HOME : ${REPO_HOME}

LIB=${REPO_HOME}/lib
if [ "$LOG_DIR" == "" ]; then
    LOG_DIR=${REPO_HOME}/logs
fi

if [ "$CONF_DIR" == "" ]; then
    CONF_DIR=$REPO_HOME/conf
fi

CLASS_PATH=""
REPO_CONF=$CONF_DIR
# prepend conf dir to classpath
if [ -n "$REPO_CONF" ]; then
  CLASS_PATH="$REPO_CONF:$CLASS_PATH"
fi

CLASS_PATH=${CLASS_PATH}:${LIB}/'*'

if [ "$REPO_PID_DIR" = "" ]; then
  REPO_PID_DIR=/tmp
fi
log=$REPO_PID_DIR/repo.out
pid=$REPO_PID_DIR/repo.pid
STOP_TIMEOUT=${STOP_TIMEOUT:-3}

JAVA_OPT="-Xms2048m -Xmx4096m"

case $actionCmd in

  (start)
    [ -w "$REPO_PID_DIR" ] ||  mkdir -p "$REPO_PID_DIR"

    if [ -f $pid ]; then
      if kill -0 `cat $pid` > /dev/null 2>&1; then
        echo $command running as process `cat $pid`.  Stop it first.
        exit 1
      fi
    fi

    echo starting $command logging to $log
    echo LOG_DIR : ${LOG_DIR}
    java ${JAVA_OPT} -cp ${CLASS_PATH} -Dlog.dir=${LOG_DIR} com.seoeun.server.RepoServer ${actionCmd} > "$log" 2>&1 < /dev/null &

    echo $! > $pid
    TARGET_PID=`cat $pid`
    echo starting as process $TARGET_PID
    ;;
  (stop)

    if [ -f $pid ]; then
      TARGET_PID=`cat $pid`
      if kill -0 $TARGET_PID > /dev/null 2>&1; then
        echo stopping $command
        kill $TARGET_PID
        sleep $STOP_TIMEOUT
        if kill -0 $TARGET_PID > /dev/null 2>&1; then
          echo "$command did not stop gracefully after $STOP_TIMEOUT seconds: killing with kill -9"
          kill -9 $TARGET_PID
        fi
      else
        echo no $command to stop
      fi
      rm -f $pid
    else
      echo no $command to stop
    fi
    ;;

  (*)
    echo $usage
    exit 1
    ;;

esac
