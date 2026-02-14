#!/bin/bash
## ---------------------------------------------------------------------------
## Execute this script as user postgres:
## 1. sudo su - postgres
## 2. cd /export/bfabric/bfabric/source/application/src/misc/scripts
## 3. ./created-db-roles.sh
## ---------------------------------------------------------------------------

PSQL="psql -t -q -U postgres -d postgres -c"

echo "======= Creating Role bfabric =============================";
$PSQL "CREATE ROLE bfabric LOGIN PASSWORD 'changeit' SUPERUSER INHERIT CREATEDB NOCREATEROLE;"

echo "======= Creating Role bfabricro ===========================";
$PSQL "CREATE ROLE bfabricro LOGIN PASSWORD 'changeit' NOSUPERUSER INHERIT NOCREATEDB NOCREATEROLE;"
$PSQL "ALTER ROLE bfabricro SET default_transaction_read_only='true';"
