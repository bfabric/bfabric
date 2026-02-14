# B-Fabric Documentation

## GlassFish logging

* Configuration file: \$GLASSFISH_HOME/glassfish/domains/domain1/config/logging.properties

* Recommended default: keep the global level at INFO and the GFFileHandler level at INFO for demo, test, and production environments.

* Runtime changes: log levels can be adjusted at runtime without redeploying; temporarily set specific loggers to FINE in logging.properties for quick debugging.

* Development helper: use ./set_log_levels.sh [dev|prod] to apply the preset log level configuration.


```
*----------+----------*----------*
|          |   dev    |   prod   |
*----------+----------*----------*
| level | INFO | INFO |
*----------+----------*----------*
| org.bfabric.level | FINE | INFO |
*----------+----------*----------*
| com.sun.enterprise.server.logging.GFFileHandler.level | FINE | INFO |
*----------+----------*----------*
| java.util.logging.ConsoleHandler.level | INFO | INFO |
*----------+----------*----------*
| javax.enterprise.resource.webcontainer.jsf.application.level | INFO | SEVERE |
*----------+----------*----------*
| javax.enterprise.resource.webcontainer.jsf.flash.level | INFO | SEVERE |
*----------+----------*----------*
| org.apache.fop.apps.FOUserAgent.level | INFO | SEVERE |
*----------+----------*----------*
```
