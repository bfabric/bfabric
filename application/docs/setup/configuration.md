# B-Fabric Documentation

## Configuration

This guide explains how to edit B-Fabric configuration values stored in the database. Completing these steps is required for the system to run correctly.

**Local installs:** If you set up a local instance by running the `load-dump` script, no manual configuration is required.

Where configuration is stored
- Contexts are defined in table `CONTEXTPROPERTY`. Default contexts include:
    - `deployer`: DEFAULT
    - `environment`: Demo, Local, Production, Test
    - `instance`: default
    - Each context property can be extended as needed.
- System settings are stored in table `SYSTEMPROPERTY`. Some entries show `TO BE SET` and must be filled in for your deployment; other values may need adjusting.

How to edit properties
- Start B-Fabric and log in as an account with the `admin` role.
- Open the admin UI: More → System Maintenance → System Property.
- Select the desired context (deployer / environment / instance) and update properties there.
- If a property is not defined for the selected context, the system will use the default value.

Notes
- Set all `TO BE SET` properties before using the system in production (emails, addresses, credentials, etc.).
- Use the B-Fabric UI to switch contexts and to change properties at runtime.

```
*-----------+-----------+-----------+
|| Name     || Value    || Comment  |
*-----------+-----------+-----------+
| aaiLoginEnabled | true/false | Switch on/off AAI login |
*-----------+-----------+-----------+
| aaiResourceEntityId | TO BE SET | The aai resource entity id |
*-----------+-----------+-----------+
| absencesMailAddress | TO BE SET | The absences email address |
*-----------+-----------+-----------+
| accessRequestManagerEmail | TO BE SET (only when accessRequestEnabled) | Email of the access request manager at UZH to sent access request |
*-----------+-----------+-----------+
| accessRequestEnabled | true/false | Access request functionality enabled |
*-----------+-----------+-----------+
| accessRequestNotificationEmail | TO BE SET (only when accessRequestEnabled) | Email address to which the notification after access request approval should be sent |
*-----------+-----------+-----------+
| accessRequestPassword | TO BE SET (only when accessRequestEnabled) | Password of the access request manager at UZH to sent access request |
*-----------+-----------+-----------+
| accessRequestUZHEmail | TO BE SET (only when accessRequestEnabled) | Email address of the access request management at UZH |
*-----------+-----------+-----------+
| addAffiliationByUserEnabled | true/false | Can users add new affiliations (institutes etc.) |
*-----------+-----------+-----------+
| applicationName | B-Fabric | Name under which the Web application is presented |
*-----------+-----------+-----------+
| askOldPasswordOnChangeRequest | true/false | Ask for old password when changes the login |
*-----------+-----------+-----------+
| bashAbsolutePath | /bin/bash | The absolute path of the bash |
*-----------+-----------+-----------+
| contractExpiryReminderJobEnabled | true/false | Switch on/off the contract expiry reminder job |
*-----------+-----------+-----------+
| coordinatorEmail | TO BE SET | Email address of the application coordinator |
*-----------+-----------+-----------+
| defaultBookingIssuerEmail | TO BE SET | The default email address of the booking issuer |
*-----------+-----------+-----------+
| defaultBookingIssuerId | TO BE SET | User ID of the default booking issuer who must have the role BOOKINGISSUER |
*-----------+-----------+-----------+
| defaultMasterExecutableIdWrapperCreator | 0 | The default master executable for executables with context "wrappercreator" |
*-----------+-----------+-----------+
| deleteDeletableUserJobEnabled | false | deleteDeletableUserJobEnabled |
*-----------+-----------+-----------+
| deleteUnassignedObjectsJobEnabled | false | deleteUnassignedObjectsJobEnabled |
*-----------+-----------+-----------+
| deleteExpiredShibbolethMappingsEnabled | false | deleteExpiredShibbolethMappingsEnabled |
*-----------+-----------+-----------+
| deployerAddress | TO BE SET | The address (street) of the deployer |
*-----------+-----------+-----------+
| deployerCity | TO BE SET| The city of the deployer |
*-----------+-----------+-----------+
| deployerCountry | TO BE SET | The country of the deployer |
*-----------+-----------+-----------+
| deployerDefaultEmail | TO BE SET | Default used when an email system property is null; leave it null for the production environment and set it for all local/test environments to ensure that emails are not sent out unintentionally
*-----------+-----------+-----------+
| deployerEmail | TO BE SET | The email of the deployer |
*-----------+-----------+-----------+
| deployerHomeURL | TO BE SET | The full home URL of the deployer which among others will be used in the footer |
*-----------+-----------+-----------+
| urlIntranet | TO BE SET | URL pointing at the Intranet of the deployer |
*-----------+-----------+-----------+
| deployerName | Functional Genomics Center Zurich | The full name of the deployer which among others will be used in the footer |
*-----------+-----------+-----------+
| deployerPhoneNumber | TO BE SET | The phone number of the deployer |
*-----------+-----------+-----------+
| deployerZip | TO BE SET | The zip of the deployer |
*-----------+-----------+-----------+
| doiPrefix | TO BE SET | The DOI prefix |
*-----------+-----------+-----------+
| doiUrlModified | true/false | Is the DOI Url modified |
*-----------+-----------+-----------+
| downloadEnabled | true/false | Switch on/off download functionality |
*-----------+-----------+-----------+
| downloadManagerEnabled | true/false | Switch on/off download manager |
*-----------+-----------+-----------+
| extensionReportReminderJobEnabled | true/false | Should extension report reminders be sent out (daily) |
*-----------+-----------+-----------+
| fromEmailAddress | do-not-reply@... | From email address for emails sent by the application |
*-----------+-----------+-----------+
| indexPath | b-fabric-index/ | The path where the index files are placed; a relative path is placed under $GLASSFISH_HOME/domains/domain1/config |
*-----------+-----------+-----------+
| lastCompiled | TO BE SET | The last compile time |
*-----------+-----------+-----------+
| lastDeployed | TO BE SET | The last deployment time |
*-----------+-----------+-----------+
| lastRevision | TO BE SET | The last revision number |
*-----------+-----------+-----------+
| listingRows | 10 | The row count to list in a table |
*-----------+-----------+-----------+
| localRepositoryPath | b-fabric-repo/ | The path where the local repository (e.g. containing the attachment files) is placed; a relative path is placed under $GLASSFISH_HOME/domains/domain1/config |
*-----------+-----------+-----------+
| loginAutoComplete | on/off | Shall autocomplete for login fields (uid/pwd) be enabled (on) or disabled (off) |
*-----------+-----------+-----------+
| logPageAccesses | true/false | Shall each page access logged in the system log |
*-----------+-----------+-----------+
| mailEnabled  | true/false |  Shall emails be sent for notification |
*-----------+-----------+-----------+
| mailSubjectPrefix | [...] | Prefix added to the subject of each email |
*-----------+-----------+-----------+
| masterExecutableIdUserSync | 68 | The executable responsible for user account synchronization |
*-----------+-----------+-----------+
| masterExecutableIdContainerSync | 68 | The executable responsible for container member account synchronization |
*-----------+-----------+-----------+
| maxAttachmentSize | 52428800 | Max attachment size in bytes |
*-----------+-----------+-----------+
| maxLoginAttempts | 3 | Max Login Attempts (0 will not restrict login attempts |
*-----------+-----------+-----------+
| offerValidityDuration | 90 | Offer validity duration in days |
*-----------+-----------+-----------+
| pollInterval | 2000 | The polling interval to keep session alive |
*-----------+-----------+-----------+
| pubtktGeneratorFilePath | /srv/bfabric/conf/mkpubtkt.sh | The script file path that  is used for Single Sign-On ticket generation |
*-----------+-----------+-----------+
| pwEncPublicKeyFilePath | /srv/bfabric/conf/rsa_key.pub | The public RSA-key file path, which is used for passwordAD enc |
*-----------+-----------+-----------+
| resetUserAvailableJobEnabled | false | resetUserAvailableJobEnabled |
*-----------+-----------+-----------+
| reviewRequired  | true/false | Is a Project Review required |
*-----------+-----------+-----------+
| sessionTimeoutWarningTime | 300 | Time (in seconds) before session timeout to show warning panel |
*-----------+-----------+-----------+
| supportEmail | TO BE SET | Email address for application support |
*-----------+-----------+-----------+
| synchronizeWithADEnabled  | true/false |  Synchronization with external authentication database (AD) enabled |
*-----------+-----------+-----------+
| technicalSupportEmail | TO BE SET | Email address for technical support |
*-----------+-----------+-----------+
| temporaryRepositoryPath | /tmp/ | Get the path to the temporary repository (place where attachment files u.a. are stored temporarily) |
*-----------+-----------+-----------+
| measureCallsFilterEnabled | false | measureCallsFilterEnabled |
*-----------+-----------+-----------+
| trustAllCertificates | false/true | Whether to trust all remote hosts or respect a known-hosts file |
*-----------+-----------+-----------+
| urlFAQ | TO BE SET | URL pointing to the FAQ |
*-----------+-----------+-----------+
| urlProjectDescriptionGuidelines | TO BE SET | URL pointing to the user manual |
*-----------+-----------+-----------+
| urlTermAndContions | TO BE SET | URL pointing to the terms and conditions |
*-----------+-----------+-----------+
| urlUserManual | TO BE SET | URL pointing to the user manual |
*-----------+-----------+-----------+
| urlUserManual | TO BE SET | URL pointing to the user manual |
*-----------+-----------+-----------+
| userRegistrationEnabled  | true/false |  Switch on/off user registration |
*-----------+-----------+-----------+
| virusScannerDisabled | false/true | Set to TRUE if the virus scanner shall be disabled to scan uploads (via browser) |
*-----------+-----------+-----------+
```