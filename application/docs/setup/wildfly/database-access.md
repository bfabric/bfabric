# B-Fabric Documentation

## Wildfly Server: Database Access Configuration

Navigate to Deployments and click on "Add" Button.

![webapp](images/Deploy_PGDriver_1.png)

Select "Upload a new deployment" and click "Next".

![webapp](images/Deploy_PGDriver_2.png)

Choose the PostgreSql Driver file (eg: postgresql-9.4-1201.jdbc41.jar), and click "Next".

![webapp](images/Deploy_PGDriver_3.png)

Change the name and runtime name into "postgres" and click "Finish".

![webapp](images/Deploy_PGDriver_4.png)

Navigate to Configuration → Subsystems → Datasources → Non-XA

Click on the "Add" button to add a new datasource.

![webapp](images/Wildfly_datasource_1.png)

Choose "PostgreSQL Datasource" and click next.

![webapp](images/Wildfly_datasource_2.png)

Fill up the name and JNDI name with "bfabric_datasource" and "java:/jdbc/bfabric_datasource" respectively. Click next.

![webapp](images/Wildfly_datasource_3.png)

In the JDBC Driver section click on "Detected Driver" and choose "postgres" from the list of already deployed drivers. Click next.

![webapp](images/Wildfly_datasource_4.png)

Fill out the database name in the Connection URL (eg: bfabric_test) and then a valid username and password for the database access.

![webapp](images/Wildfly_datasource_5.png)

Before Finish, click on "Test Connection" to check if all data was introduced correctly.

![webapp](images/Wildfly_datasource_6.png)