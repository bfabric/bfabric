# B-Fabric Documentation

## Mail Delivery: Postfix Mail Client

Payara Server is responsible for sending mail. Please check the Payara Server Mail configuration first!

Mainly for debugging purpose you can configure the Postfix Mail Transport Agent (link) directly on the B-Fabric host and use it between B-Fabric and your smtp-relay host. The logfiles can be found in
/var/log.

This page contains the guide for installing/configuring the http://www.postfix.org/ Postfix Mail Transport Agent, so it can be utilized in B-Fabric system. B-Fabric system can be started without this
component. If you set up GlassFish Mail exactly as described in another part of this website, the mail delivering functionality will not be available without the postfix server. The successful
completion of this part of the guide is not crucial for the system to run.

This configuration will set up a closed mail server, which allows connections only from localhost.

* Prerequisite: Before you setup your own mailserver, please contact your sites mail administrator.

* SMTP host which will be configured in bfabric must be linked to "localhost" in /etc/hosts.

```
127.0.0.1       bfabricmail    bfabric-host    localhost.localdomain   localhost
```

* Install the package "postfix".

 ```
root@bfabric-host:~# apt install postfix
``` 

* As general type of mail configuration, choose "satellite system".

* When prompted with "System mail name" put the following: your_ip, localhost.uzh.ch, localhost

* Then add the site SMTP server as smart host.

* Make sure that following sample parameters are present in /etc/postfix/main.cf file.

 ``` 
mydestination = bfabric-host.yourdomain.com, localhost.yourdomain.com, localhost
relayhost = smtp.yourdomain.com
mynetworks = 127.0.0.0/8 [::ffff:127.0.0.0]/104 [::1]/128
inet_interfaces = loopback-only
``` 

* After editing the configuration file, restart the application in order to apply the changes.

 ```
root@bfabric-host:~# /etc/init.d/postfix restart
 * Stopping Postfix Mail Transport Agent postfix
   ...done.
 * Starting Postfix Mail Transport Agent postfix
   ...done.
``` 

The configuration for local(development) looks as follows:

* Sometimes developer needs to check if an email is delivered to the expected list of users or the content contains the expected format template and information.

* In such cases, a local mail server must be configured so that access to outgoing email is possible locally.

* For this purpose run the following commands as normal user

``` 
bfabricdeveloper@bfabric-host:~# mkdir $HOME/MAILBOX
bfabricdeveloper@bfabric-host:~# sudo vim /etc/postfix/main.cf
```

* Copy the following content into the file /etc/postfix/main.cf .

```
smtpd_banner = $myhostname ESMTP $mail_name (Ubuntu)
biff = no
append_dot_mydomain = no
readme_directory = no
smtpd_relay_restrictions = permit_mynetworks permit_sasl_authenticated defer_unauth_destination
alias_maps = hash:/etc/aliases
alias_database = hash:/etc/aliases
myorigin = /etc/mailname
mynetworks = 127.0.0.0/8 [::ffff:127.0.0.0]/104 [::1]/128
mailbox_size_limit = 0
recipient_delimiter = +
inet_interfaces = loopback-only
inet_protocols = all
append_at_myorigin = yes
home_mailbox = MAILBOX/Inbox
virtual_alias_maps = static:matthias@localhost
myhostname = localhost
```

* Run the following command to install mail client to test sending email from command line

```
bfabricdeveloper@bfabric-host:~# sudo get install bsd-mailx
bfabricdeveloper@bfabric-host:~# sudo newaliases
bfabricdeveloper@bfabric-host:~# sudo systemctl restart postfix.service
bfabricdeveloper@bfabric-host:~# date | mail -s test EMAIL_ADDRESS
```

* If the above worked well the file "Inbox" is created in $HOME/MAILBOX, you will notice in "Inbox", "Delivered" from ... and "to" set to EMAIL_ADDRESS

* In case the file Inbox is not created, or it does not contain the expected content, check the log files /var/log/mail.log and cat /var/log/mail.err for troubleshooting.