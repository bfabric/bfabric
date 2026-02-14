# B-Fabric Documentation

## Ldap Configuration

This page explains how to configure GlassFish so it can connect to the LDAP server over SSL.

### Development Setup

* Import the server certificate `./....cer` into GlassFish's keystore at `glassfish/domains/domain1/config/cacerts.jks` to enable SSL for LDAP. The default keystore password is `changeit`.

```
# cd glassfish/domains/domain1/config
# keytool -import -v -alias ... -file ....cer -keystore cacerts.jks
```

* Make sure to adapt the bfabricOverride.properties section:

```
# Ldap
contextSource.url=ldaps://192.168.81.101
contextSource.base=DC=bfabric,DC=uzh,DC=ch
contextSource.userDn=CN=ldapadmin,OU=OU_Applications,OU=OU_Accounts,DC=bfabric,DC=uzh,DC=ch
contextSource.password=secret
ldapClient.enabled=true
ldapClient.baseDn=DC=bfabric,DC=uzh,DC=ch
ldapClient.userDn=OU=OU_Users,OU=OU_Accounts
ldapClient.userGroup=CN=SG_Users,OU=OU_Groups
ldapClient.employeeDn=OU=OU_Employees,OU=OU_Accounts
ldapClient.employeeGroup=CN=SG_Employees,OU=OU_Groups
ldapClient.projectDn=OU=OU_Projects,OU=OU_Groups
ldapClient.domain=bfabric.uzh.ch
ldapClient.nisDomain=bfabric
ldapClient.homeDrive=x:
ldapClient.homeDirectoryBase=\\\\your_home_directory_base\\Users
ldapClient.unixUserGroupId=10152
ldapClient.unixEmployeeGroupId=10147
```

### Production Setup

* To enable LDAP over SSL in production, import the server certificate `./....cer` into GlassFish's keystore at `glassfish/domains/domain1/config/cacerts.jks`. The keystore's default password is `changeit`.

 ```
# cd glassfish/domains/domain1/config
# keytool -import -v -alias ... -file ....cer -keystore cacerts.jks
 ```

* Make sure to adapt the bfabricOverride.properties section:

```
# Ldap
contextSource.url=ldaps://your_ldap_ip_address
contextSource.base=DC=your_DC,DC=unizh,DC=ch
contextSource.userDn=CN=ldapadmin,OU=OU_Applications,OU=OU_Accounts,DC=your_DC,DC=unizh,DC=ch
contextSource.password=secret
ldapClient.enabled=true
ldapClient.baseDn=DC=your_DC,DC=unizh,DC=ch
ldapClient.userDn=OU=OU_Users,OU=OU_Accounts
ldapClient.userGroup=CN=SG_Users,OU=OU_Groups
ldapClient.employeeDn=OU=OU_Employees,OU=OU_Accounts
ldapClient.employeeGroup=CN=SG_Employees,OU=OU_Groups
ldapClient.projectDn=OU=OU_Projects,OU=OU_Groups
ldapClient.domain=your_DC.unizh.ch
ldapClient.nisDomain=your_DC
ldapClient.homeDrive=x:
ldapClient.homeDirectoryBase=\\\\your_home_directory_base\\Users
ldapClient.unixUserGroupId=10152
ldapClient.unixEmployeeGroupId=10147
```