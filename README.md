# NoteTaker
Note taking app for MobileDay

## Requirements

This web app was built and tested with the following:

* Java 8
* Tomcat 7
* Maven 3


## To Build

Clone this repository
<pre>
cd NoteTaker
mvn clean package
cp target/NoteTaker.war {TOMCAT_HOME}/webapps
</pre>

## To Configure

The app uses an embedded db (hsqldb) which is configured to exist in /tmp/NoteTaker.

If you want to put that somewhere else, choose a directory that is read/writable by Tomcat.

Edit the file:  NoteTaker/src/main/resources/noteTaker.properties

The property (the only one!) is called:  cache.dir

## To Access

Point your browser to:   <code> http://{TOMCAT URL}/NoteTaker/note  </code>
