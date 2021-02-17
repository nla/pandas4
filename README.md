PANDAS 4
========

The fourth-generation of the PANDAS web archiving workflow system. This project
is at an early stage development and currently must be used in conjunction with
the older PANDAS 3 system (which is not open source).

Building and Running
--------------------

To compile install [JDK 11](https://adoptopenjdk.net/?variant=openjdk11&jvmVariant=hotspot) and [Maven](https://maven.apache.org/)

    mvn package

Run the package jar with:

    java -jar ui/target/pandas-admin.jar

Configuration
-------------

Set the following environment variables:

```sh
## Database details
#PANDAS_DB_URL=
#PANDAS_DB_USER=
#PANDAS_DB_PASSWORD=

## Webapp details
# PORT=3001
# CONTEXT_PATH=/admin

## Path to store lucene indexes
#DATA_PATH=/tmp/data

## OpenID Connect authentication (optional)
#OIDC_URL=
#OIDC_CLIENT_ID=
#OIDC_CLIENT_SECRET=
```
