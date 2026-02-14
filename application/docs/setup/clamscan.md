# B-Fabric Documentation

## Clamscan

This page explains how to install ClamAV's `clamscan` antivirus and configure it for use with B-Fabric. Complete these steps to ensure `clamscan` is installed and properly configured; they are required for the system to run.

```
root@bfabric-host:~# apt-get install clamav
...
#Install the clamav-deamon as well.
root@bfabric-host:~# apt-get install clamav-daemon
...
root@bfabric-host:~# which clamscan
/usr/bin/clamscan
```

## Configuring ClamAV Daemon

By default, the ClamAV daemon does not create the TCP socket required by B-Fabric. Append the following lines to the end of /etc/clamav/clamd.conf to enable it:

```
# TCP port address.
# Default: no
TCPSocket 3310
 
# TCP address.
# By default we bind to INADDR_ANY, probably not wise.
# Enable the following to provide some degree of protection
# from the outside world.
# Default: no
TCPAddr 127.0.0.1
 
# Close the connection when the data size limit is exceeded.
# The value should match your MTA's limit for a maximum attachment size.
# Default: 25M
StreamMaxLength 100M
```

Or, the correct way to change the settings via command line would be:

 ```
root@bfabric-host:~# dpkg-reconfigure clamav-daemon
``` 

Once this is done, you should restart the clamd service using the following line

```
root@bfabric-host:~# service clamav-daemon restart
```

This will ask many configuration questions and build up all the config-files needed.

## Troubleshooting Steps that worked

If the above steps do not work, you can try the following:

create the socket file manually

```
root@bfabric-host:~# mkdir /etc/systemd/system/clamav-daemon.socket.d
root@bfabric-host:~# touch /etc/systemd/system/clamav-daemon.socket.d/bfabric.conf
```

Then add the following lines to the file:

```
[Socket]
ListenStream=127.0.0.1:3310
```

Then restart the clamav-daemon service:

```
root@bfabric-host:~# systemctl restart daemon-reload
root@bfabric-host:~# systemctl restart clamav-daemon
root@bfabric-host:~# systemctl restart clamav-daemon.socket
```

Then check the status of the socket:

```
root@bfabric-host:~# systemctl status clamav-daemon.socket
```

Then check the status of the service:

```
root@bfabric-host:~# systemctl status clamav-daemon
```