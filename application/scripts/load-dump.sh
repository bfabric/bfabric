#!/bin/bash
## ---------------------------------------------------------------------------
## Note: This script requires that the script create-db-roles.sh had been executed successfully.
## ---------------------------------------------------------------------------
## Note: execute following command to create new initial  DB dump:
## pg_dumpall -c -U bfabric | gzip > sql/bfabric-initial.sql.gz
## ---------------------------------------------------------------------------

MAINTENANCE_USER=bfabric

DB=bfabric_test
BFABRIC_DUMP=sql/bfabric-dump.sql.gz
BFABRIC_INITIAL=sql/bfabric-initial.sql.gz

BFABRIC_DUMP_UPDATE=sql/bfabric-dump-update.sql
BFABRIC_INITIAL_UPDATE=sql/bfabric-initial-update.sql
BFABRIC_SETTINGS_UPDATE_INITIAL=sql/bfabric-settings-update-initial.sql
BFABRIC_REFRESH_ALL_MATERIALIZED_VIEWS=sql/bfabric-refresh-all-materialized-views.sql

PSQL="psql -q -t -U $MAINTENANCE_USER"

## Initialize start time.
START_TIME=$SECONDS
ELAPSED_TIME=$SECONDS

printElapsedTime() {
  echo "======================================================= $(($PRINT_TIME / 60)) min $(($PRINT_TIME % 60)) sec"
}

errorCheck() {
  ELAPSED_TIME=$(($SECONDS - $ELAPSED_TIME))
  PRINT_TIME=$ELAPSED_TIME
  printElapsedTime
  if [ $? -gt 0 ]; then
    echo "=== DATABASE LOAD FAILED =============================="
    exit 1
  fi
}

echo "======================================================= $(date +"%T")"

case "$1" in
dump) ;;

initial)
  if [ ! -r $BFABRIC_INITIAL ]; then
    echo "=== DATABASE INITIAL FILE MISSING ====================="
    exit 1
  fi
  echo "=== USING DATABASE INITIAL FILE AS DUMP ==============="
  BFABRIC_DUMP=$BFABRIC_INITIAL
  BFABRIC_DUMP_UPDATE=$BFABRIC_INITIAL_UPDATE
  ;;
*)
  echo $"Usage: $0 {dump|initial}"
  exit 1
  ;;
esac

if [ ! -r $BFABRIC_DUMP ]; then
  echo "=== DATABASE DUMP FILE MISSING ========================"
  exit 1
fi

if [ ! -r $BFABRIC_DUMP_UPDATE ]; then
  echo "=== $BFABRIC_DUMP_UPDATE FILE MISSING ================="
  exit 1
fi

## First elapsed time computed based on start time; then based on previous elapsed time.
errorCheck

echo "=== DROP DATABASE IF EXISTS ==========================="
$PSQL -d postgres -c "DROP DATABASE IF EXISTS $DB"
errorCheck

echo "=== LOAD DATABASE DUMP ================================"
gzip -cd -f "$BFABRIC_DUMP" | $PSQL -d postgres >/dev/null
errorCheck

if [ "$1" == "initial" ]; then
  echo "=== LOAD BFABRIC_SETTINGS_UPDATE_INITIAL =============="
  $PSQL -d $DB <$BFABRIC_SETTINGS_UPDATE_INITIAL
  errorCheck
fi

echo "=== DATABASE SUCCESSFULLY LOADED ======================"

PRINT_TIME=$(($SECONDS - $START_TIME))
printElapsedTime

echo ""
echo "Press any key to close this window"
read -r -n 1