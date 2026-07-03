#!/bin/sh

otilmHome="/opt/otilm"
source ${otilmHome}/static-functions

log "INFO" "Launching the Common Credential Provider"
java $JAVA_OPTS -jar ./app.jar

#exec "$@"