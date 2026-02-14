# B-Fabric Documentation

## GlassFish Server: Installation Backup

If you have completed the previous steps, GlassFish is ready to use.

The server configuration is stored in `\$GLASSFISH_HOME/domains/domain1/config/domain.xml`.

Because GlassFish can fail and corrupt files inside the domain directory, troubleshooting the exact cause can be time-consuming. To avoid that, make a backup of the freshly configured domain directory now. If the domain becomes damaged later, restore this backup instead of hunting for the problem.

The steps below explain how to create that backup.

* Stop the GlassFish Server.

```
bfabric@bfabric-host:~$ ./$GLASSFISH_HOME/bin/asadmin stop-domain domain1
```

* Delete the content of the log file.

```
bfabric@bfabric-host:~$ rm $GLASSFISH_HOME/domains/domain1/server.log
 ```

* Create the backup directory.

```
bfabric@bfabric-host:~$ cp -r $GLASSFISH_HOME /home/bfabric/glassfish.backup
```