# B-Fabric Installation Scripts

***

### Database Configuration

* Create database roles: execute the script create-db-roles.sh as user postgres

    * `sudo su - postgres`
    * `cd /export/bfabric/bfabric/source/application/src/misc/scripts`
    * `./created-db-roles.sh`


* Load database dump: execute the load-dump with the corresponding option

    * `dump` : locally available production database dump
    * `initial` : initial database dump

***

### Create Local Repositories

* B-Fabric stores all uploaded data files in three local folders (repositories):
    * `internal` : stores files belonging to entities that can only be accessed by internal users
    * `external` : stores files belonging to entities that can be accessed by all users
    * `temp` : used for caching local file uploads


* Execute the following script, named create-local-repo.sh, to create these folders:

```
  sudo mkdir -p /export/bfabric/data/b-fabric-internal-repo
  sudo mkdir -p /export/bfabric/data/b-fabric-external-repo
  sudo mkdir -p /export/bfabric/data/tmp
  sudo chown -R $(id -u):$(id -g) /export/bfabric/data
```

* On a Windows development machine, unzip the file `$BFABRIC_CODE/application/src/misc/scripts/export.zip` and place the unzipped folder `export` under the root folder C:\ instead of executing the
  script above.

***

### Set Log Levels

* Run following script for setting the log level configuration either to 'dev' or 'prod':

  `./set_local_log_levels [dev|prod]`


* Some configurations:

  | Level                                                        | dev   | prod   | 
      |-------|--------| ---- |
  | .level                                                       | INFO  | INFO   |
  | org.bfabric.level                                            | FINE  | INFO |
  | com.sun.enterprise.server.logging.GFFileHandler.level        | FINE  | INFO   |
  | java.util.logging.ConsoleHandler.level                       | INFO  | INFO   |
  | javax.enterprise.resource.webcontainer.jsf.application.level | INFO  | SEVERE |
  | javax.enterprise.resource.webcontainer.jsf.flash.level       | INFO  | SEVERE |
  | org.apache.fop.apps.FOUserAgent                              | INFO  | SEVERE |


* Note: The instance must be running.