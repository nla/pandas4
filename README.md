PANDAS 4
========

The fourth-generation of the PANDAS web archiving workflow system. While open source
this is not yet documented or packaged for use outside our (NLA) 
infrastructure.

External Dependencies
---------------------

PANDAS requires the following tools to be installed:

* [JDK 17](https://adoptium.net/)
* [Maven](https://maven.apache.org/)
* [pywb](https://github.com/webrecorder/pywb)
* [Ghostscript](https://www.ghostscript.com/)

On RHEL/CentOS 7 install with:

    yum install -y epel-release
    yum install -y ghostscript httrack java-11-openjdk-devel python36 python36-devel python36-setuptools
    pip install pywb

Building and Running
--------------------

Compile with:

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

## Browsertrix backend
#BROWSERTRIX_WORKERS=4
#BROWSERTRIX_PAGE_LIMIT=1000
#BROWSERTRIX_USER_AGENT_SUFFIX=
```~~~~
