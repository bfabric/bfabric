# B-Fabric Documentation

## Code Compilation

For compiling B-Fabric, Java and Maven must be installed. The successful completion of this part of the guide is crucial for the system to run.

### Java Installation

* Install http://www.java.com/ Java Version 8 to run the current B-Fabric Version 10. The instructions for the OpenJDK installation can be found http://openjdk.java.net/install/index.html here. It is
  important to install Java Development Kit (JDK), not only Java Runtime Environment (JRE).

```
sudo apt install openjdk-8-jdk
```

* After finishing the JDK installation, set the environment variable $JAVA_HOME by editing the /etc/profile file (it must contain the path to the directory in which you installed java).

* Include the $JAVA_HOME/bin in your $PATH environment variable by editing the /etc/profile file.

* Check from where your java and javac executables are taken using the "which" command:

```
which java
which javac
```

* Configure the default for the program "java" (Java VM) and default Java compiler using the following command:

```
sudo update-alternatives --config java
sudo update-alternatives --config javac
```

### Maven Installation

* Install the https://maven.apache.org/ Maven tool, version 3.0.4 or higher.

```
apt-get install maven3
```

### Code Compilation

Change into the directory where the code is located and build the project root:

```
cd $BFABRIC_CODE
mvn clean install
```

A successful build shows BUILD SUCCESS. If you see BUILD FAILURE, inspect the Maven output to find the error and verify you have the correct JDK installed.

Build the web application module:

```
cd $BFABRIC_CODE/application/
mvn clean install
```  

A second BUILD SUCCESS indicates the WAR file was produced in the module's target directory.


Notes:

* Maven reads pom.xml in the current directory and places compiled artifacts under target.

* target directories are not under version control and can be safely deleted and recreated by running `mvn clean install`.


### Useful Aliases

For your convenience, set and use the following environment variable and aliases:

```
## Set the directory where the B-Fabric source is checked-out locally.
export BFABRIC_CODE=... 

# Build the full project then build the web application WAR.
alias mci="cd $BFABRIC_CODE && rm -rf target && mvn clean install && cd application/ && rm -rf target && mvn clean install"

# Build the download manager. (Optionally copy the produced JAR into the application's fragments folder.)
alias mdm="cd $BFABRIC_CODE/downloadmanager && rm -rf target && mvn clean install" 

# Generate the site/docs for the web application.
alias docs="cd $BFABRIC_CODE/application/ && mvn clean site"
```  