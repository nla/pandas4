PANDAS 4
========

The fourth-generation of the PANDAS web archiving workflow system. This project
is at an early stage development and currently must be used in conjunction with
the older PANDAS 3 system (which is not open source).

Building and Running
--------------------

To build and run out of the source tree use:

    mvn spring-boot:run -Dspring-boot.run.arguments=--spring.jpa.hibernate.ddl-auto=update

To package as a single jar file for easy deployment use:

    mvn package spring-boot:repackage

You can run the packaged jar with:

    java -jar target/pandas-admin-spring-boot.jar

Configuration
-------------

Set the following environment variables:

```sh
PANDAS_DB_URL=
PANDAS_DB_USER=
PANDAS_DB_PASSWORD=
# PORT=3001
```
