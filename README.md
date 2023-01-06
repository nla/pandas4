PANDAS 4
========

The fourth-generation of the PANDAS web archiving workflow system. While open source
this is not yet documented or packaged for use outside our (NLA) 
infrastructure.

PANDAS provides a user interface for curators to perform website selection, collection building,
scheduled crawling and quality assuarance using various web crawlers (Heritrix, Browsertrix, HTTrack).

External Dependencies
---------------------

PANDAS requires the following tools to be installed:

* [JDK 17](https://adoptium.net/)
* [Maven](https://maven.apache.org/)
* [pywb](https://github.com/webrecorder/pywb)
* [Ghostscript](https://www.ghostscript.com/)
* [Bamboo](https://github.com/nla/bamboo) (the NLA web archive collection manager)
* An SQL database (see next section)

On RHEL/CentOS 7 install with:

    yum install -y epel-release
    yum install -y ghostscript httrack java-11-openjdk-devel python36 python36-devel python36-setuptools
    pip install pywb

### Database Support

PANDAS is known to work with Oracle, Postgresql and MariaDB. It may work with other databases that are supported by
Hibernate and support sequences and recursive CTEs.

MySQL is assumed to currently not work due to the use of sequences. H2 is used for automated tests but is not
recommended for production due to bugs in its CTE support.

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
