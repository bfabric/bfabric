# B-Fabric — Documentation and Report Generation

The Maven Site Plugin keeps project documentation together with the code. The documentation includes JavaDoc and the site reports under `reports/index.html`. In B-Fabric, the documentation source is located at `$BFABRIC_CODE/application/src/site` and is written in APT format (see https://maven.apache.org/doxia/references/apt-format.html).

Every developer should periodically generate the site locally and fix issues they introduced.

## Generate the documentation

From the `application` directory run:
  ```
mvn clean site
```

This builds the site in HTML and writes it to `\$BFABRIC_CODE/application/target/site`.

## Viewing the documentation / reports

** Simple

The simplest possibility to view the documentation/report is to open the file $BFABRIC_CODE/application/target/site/index.html.

** 'Persistent'

As it may be annoying to lose site and report each time you do a <<"mvn clean">> you can configure you're preferred destination the documentation/reports will get deployed to, this can also be a local
destination. To do so you will need to configure the <<bfabric.distribution.site.url>> property in your
<<~/.m2/settings.xml>>:

  ```
<?xml version="1.0" encoding="UTF-8"?>
<settings xmlns="https://maven.apache.org/POM/4.0.0"
          xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
          xsi:schemaLocation="https://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/settings-1.0.0.xsd">
  <!-- ++++++++ -->
  <!-- Profiles -->
  <!-- ++++++++ -->
  <profiles>
    <profile>
      <id>bfabric-properties</id>
      <properties>
        <bfabric.distribution.site.url>file:///home/ego/www-bfabric/</bfabric.distribution.site.url>
      </properties>
    </profile>
  </profiles>
  <!-- +++++++++++++++ -->
  <!-- Active Profiles -->
  <!-- +++++++++++++++ -->
  <activeProfiles>             
    <activeProfile>bfabric-properties</activeProfile>              
  </activeProfiles>                                                          
</settings>
```

To deploy you can run either <<"mvn clean site-deploy">> to build the sites 'persistent'. You can even add the sites to the favorites in your browser.

```
file:///home/ego/www-bfabric/bfabric/index.html
file:///home/ego/www-bfabric/reports/bfabric/index.html
```

** Advanced

A more "advanced" possibility is to install the Apache Web Server on the machine and configure the file /etc/apache2/sites-enabled/000-default as follows:

```
DocumentRoot /home/bfabric/trunk/application/target/site
  <Directory /home/bfabric/trunk/application/target/site/>
        Options Indexes FollowSymLinks MultiViews
        AllowOverride None
        Order allow,deny
        allow from all
  </Directory>
```

Replace the path "/home/bfabric/trunk/application/target/site/" by your own location, then enter the IP number of your machine in the browser. 