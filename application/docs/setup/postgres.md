# B-Fabric Documentation

## PostgreSQL Database

Prerequisites

* Complete the earlier System Setup sections.

* Replace version numbers below with the PostgreSQL version you installed.

Install (Debian/Ubuntu)

```
apt install postgresql-13 postgresql-client-13
```

Optional GUI

```
apt-get install pgadmin4
```

* On a Windows development machine, create the following system environment variables

```
POSTGRES_HOME path-to-your-local-postgres-installation
PGPASSWORD changeit
```

* and add `%POSTGRES_HOME%/bin` to the `PATH` system variable.

## Database Settings for Local Installations

* Database name: "bfabric_test"

* Role for database access: "bfabric"

* Password: "changeit"

* Edit the file /etc/postgresql/16.1/main/pg_hba.conf as user "root"; make sure that following access rights are configured.

 ```
local   all         all                     trust
host    all         all     127.0.0.1/32    trust
``` 

* Reload the configuration as user "root".

``` 
sudo /etc/init.d/postgresql reload
``` 

## Create Local Repositories

* B-Fabric stores all uploaded data files in three local folders (repositories):
    * `internal` : stores files belonging to entities that can only be accessed by internal users
    * `external` : stores files belonging to entities that can be accessed by all users
    * `temp` : used for caching local file uploads

* These folders have to be created accordingly.

* On a development machine, you may execute the script `$BFABRIC_CODE/application/scripts/create-local-repo.sh`, to create these folders:

```
  sudo mkdir -p /export/bfabric/data/b-fabric-internal-repo
  sudo mkdir -p /export/bfabric/data/b-fabric-external-repo
  sudo mkdir -p /export/bfabric/data/tmp
  sudo chown -R $(id -u):$(id -g) /export/bfabric/data
```

* On a Windows development machine, unzip the file `$BFABRIC_CODE/application/scripts/export.zip` and place the unzipped folder `export` under the root folder C:\ instead of executing the
  script above.

## Load B-Fabric Database

Load either the initial or a dump B-Fabric database. The $BFABRIC_CODE/application/scripts folder contains the following scripts:

* `create-db-roles.sh`: creates the needed bfabric and bfabricro roles in the local database.

* `load-dump.sh`: loads a given B-Fabric database into the local database and performs local settings, e.g., to avoid that emails are sent out to the real email addresses of the users.

* `create-local-repo.sh`: creates local repositories on local development system.

The script create-db-roles.sh needs to be executed as user "postgres" only once when the database server is installed the first time.

```
sudo su - postgres

./$BFABRIC_CODE/application/src/misc/script/create-db-roles.sh
```

To load the initial database, run load-dump.sh with the option initial.

```
./$BFABRIC_CODE/application/src/misc/script/load-dump.sh initial
```

* Use load-dump.sh with the option dump to load the given database dump (this will take longer to complete depending on the size of your database dump).

```
./$BFABRIC_CODE/application/scripts/load-dump.sh dump
```

* Use load-dump.sh with the option fresh to get and load the latest production database dump.

```
./$BFABRIC_CODE/application/scripts/load-dump.sh fresh
```

* If the script execution finished and everything completed successfully, the last line will contain "DATABASE SUCCESSFULLY LOADED". Otherwise, the last line will contain "DATABASE LOAD FAILED".

The script create-local-repo.sh needs to be executed only once when you set up your local development system. This script will create the local repositories where B-Fabric stores all files, e.g.,
comment attachments.

```
./$BFABRIC_CODE/application/src/misc/script/create-local-repo.sh
```

## Track Required B-Fabric Database Updates

The $BFABRIC_CODE/application/src/misc/sql folder contains the following three files which need to updated and maintained consistently:

* `bfabric-initial.sql.gz` holds the gzipped B-Fabric initial database

* `bfabric-initial-update.sql` contains all commands needed to update the initial database to run the latest code version

* `bfabric-dump-update.sql` contains all commands needed to update the last production database dump to run the latest code version as well as to track which commands have already been applied on the
  production, test, and demo databases.

## Backup

Use the following command to create a gzipped dump file of the B-Fabric database.

```
  bfabric@bfabric-host:~$ pg_dumpall -c | gzip -c - > bfabric-dump.sql.gz
```

## Troubleshooting

The provided configuration allows one to connect to the database from localhost without being prompted for the password. One can check this through the "psql" tool. The following command should return
a psql prompt.

```
  bfabric@bfabric-host:~$ psql -U bfabric -d postgres
  psql (16.1)
  Type "help" for help.
  
  bfabric_test=#
```

If this does not work, check again whether /etc/postgresql/16.1/main/postgresql.conf and /etc/postgresql/16.1/main/pg_hba.conf were configured as described. Don't forget to restart the database every
time you edit those files. If the database works correctly, check the directory $BFABRIC_CODE/application/src/misc/sql, if it should contain following files, all of them must have at least read
permission.  