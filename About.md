# JDBC Code Gen #
Generate Java code based on JDBC metadata

## Setup ##
In the file src/main/resources/jdbccodegen.properties the settings for the program can be defined.

The parameters are:
  * JDBC\_DRIVER
  * JDBC\_URL
  * OUTPUT\_FOLDER
  * JDBC\_USER
  * JDBC\_PASS

When the JAR file is created the properties file is located on the root of the JAR.

## Build ##
  * mvn clean compile package

## Run for mysql ##
  * java -cp target/jdbcCodeGen-0.0.1-SNAPSHOT.jar:/tmp/mysql-connector-java-5.1.30-bin.jar org.allowed.bitarus.jdbccodegen.Main

## Run for mysql with JAR with dependencies ##
  * java -jar target/jdbcCodeGen-0.0.1-SNAPSHOT-jar-with-dependencies.jar

## Test built Java files ##
  * cd /tmp/gen
  * rm **.class
  * javac**.java
  * java -cp .:/tmp/mysql-connector-java-5.1.30-bin.jar MainApp #requires mysql-connector-java-5.1.30-bin.jar in /tmp
