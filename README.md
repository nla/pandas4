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
```

### Keycloak integration

PANDAS can use Keycloak for single sign on. It may be possible to adapt it other OpenID Connect auth servers although
account editing and access roles currently make use of Keycloak-specific features.

Versions known to work:

* Red Hat Single Sign-On 7.4 
* Keycloak 14

Configure Keycloak in this way:

1. Create a new realm called 'pandas'.
2. Create a new client for each application (e.g. pandas-ui, pandas-gatherer, pandas-delivery, bamboo etc).
   - **Access type:** confidential
   - **Standard Flow Enabled:** on
   - **Implicit Flow Enabled:** off
   - **Direct Access Grants Enabled:** off
   - **Service Accounts Enabled:** on
   - **Valid Redirect URIs:** user-facing URL of the application followed by `/*`
   
   Press save.
4. Go to the Credentials tab and copy the client secret. In each app's own configuration set 
   OIDC_URL to the realm URL (e.g. `https://localhost:8443/auth/realms/pandas` 
   and OIDC_CLIENT_ID and OIDC_CLIENT_SECRET so they match the values in Keycloak.
4. Create the following realm-level roles:
   - sysadmin
   - panadmin
   - agadmin
   - stduser
   - infouser
5. For each role set:
   - **Composite Roles:** on
   - **Realm Roles - Associated Roles:** the next lower access level (e.g. panadmin -> agadmin, agadmin -> stduser)

### Keycloak user management

If you want to be able to manage Keycloak users from within PANDAS, you'll need to grant it the manage-users permission.

1. Open the pandas client in the Keycloak realm settings.
2. Go to Settings tab.
3. Ensure 'Service Accounts Enabled' is ON.
4. Click Save.
5. Go to the Service Account Roles tab.
6. Under 'Client roles' select 'realm-management'.
7. Under 'Available roles' select 'manage-users'.
8. Click 'Add selected'.

Then set OIDC_ADMIN_URL to the save value as OIDC_URL in the PANDAS UI environment:

    OIDC_ADMIN_URL=http://keycloak.example/auth/realms/pandas
    SAVE_USER_TO_KEYCLOAK=true