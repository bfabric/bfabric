#!/bin/bash
# Script for setting the log level configuration either to 'dev' or 'prod'.
# Executing the script: ./set_local_log_levels [dev|prod]
# NOTE: The instance needs to be running.
#                                                                 dev     |     prod
#                                                                 ------------------
# - .level=                                                       INFO    |     INFO
# - org.bfabric.level=                                            FINE    |     INFO
# - com.sun.enterprise.server.logging.GFFileHandler.level=        FINE    |     INFO
# - java.util.logging.ConsoleHandler.level=                       INFO    |     INFO
# - javax.enterprise.resource.webcontainer.jsf.application.level= INFO    |     SEVERE
# - javax.enterprise.resource.webcontainer.jsf.flash.level=       INFO    |     SEVERE
# - org.apache.fop.apps.FOUserAgent=FATAL                         INFO    |     SEVERE

if [ -z "${GLASSFISH_HOME}" ]; then
  echo "GLASSFISH_HOME is not set in the PATH."
else
  if [ $# -ne 1 ]; then
    echo "Wrong number of arguments"
  else
    declare -a UPDATE_COMMAND=("${GLASSFISH_HOME}/glassfish/bin/asadmin" set-log-levels)
    if [ $1 == "dev" ]; then
      UPDATE_COMMAND+=(=INFO:com.sun.enterprise.server.logging.GFFileHandler=FINE:java.util.logging.ConsoleHandler=INFO:org.bfabric=FINE:javax.enterprise.resource.webcontainer.jsf.application=INFO:javax.enterprise.resource.webcontainer.jsf.flash=INFO:org.apache.fop.apps.FOUserAgent=INFO)
      "${UPDATE_COMMAND[@]}"
    elif [ $1 == "prod" ]; then
      UPDATE_COMMAND+=(=INFO:com.sun.enterprise.server.logging.GFFileHandler=INFO:java.util.logging.ConsoleHandler=INFO:org.bfabric=INFO:javax.enterprise.resource.webcontainer.jsf.application=SEVERE:javax.enterprise.resource.webcontainer.jsf.flash=SEVERE:org.apache.fop.apps.FOUserAgent=SEVERE)
      "${UPDATE_COMMAND[@]}"
    else
      echo "Wrong type of arguments. Allowed are 'dev' and 'prod'."
    fi
  fi
fi

exit 0