# B-Fabric Documentation

## SWITCHaai login

In order to allow SWITCHaai users to log in to B-Fabric, the application server (GlassFish) must be front-ended with a Web server (Apache).

For setting up your development environment, this SWITCHaai login setup is not required (until you have to work on this topic)!

* Install Apache Web server (not necessarily but preferably on the computer where GlassFish runs)

* Enable Apache modules: ssl, proxy_http and rewrite

* Add the following to the default-ssl file

```
<Location /bfabric>
    Order allow,deny
    allow from all
    AuthType shibboleth
    ShibUseHeaders On
    require shibboleth
  </Location>

  <Location /ShibbolethServlet>
    AuthType shibboleth
    ShibRequireSession On
    ShibUseHeaders On
    # Comment the line below unless working on the production federation
    require homeOrgType university
    # Uncomment the line below while workin on the Test federation
    #require valid-user
  </Location>
  
  <Location /bfabric/shibboleth/ShibbolethServlet>
    AuthType shibboleth
    ShibRequireSession On
    ShibUseHeaders On
    # Comment the line below unless working on the production federation
    require homeOrgType university
    # Uncomment the line below while workin on the Test federation
    #require valid-user
  </Location>

  # hostname is the url of the computer where the applicaton runs
  ProxyPass /bfabric http://<hostname>:8080/bfabric
  ProxyPassReverse /bfabric http://<hostname>:8080/bfabric
```