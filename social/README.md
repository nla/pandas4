PANDAS Social
=============

Setup
=====

### Configuring Keycloak integration

1. Create a new client called "pandas-social" in the "pandas" realm.
2. Set the access type to "confidential".
3. Enable the "service accounts" option.
4. Disable the "direct access grants" option.
5. Press the "Save" button.
6. Under the "Service Account Roles" tab, assign the "panadmin" realm role.
7. Under the "Credentials" tab, copy the "Secret" value to the `OIDC_SECRET` environment variable.

Example environment variables:

    OIDC_URL=https://keycloak.example.org/auth/realms/pandas
    OIDC_CLIENT_ID=pandas-social   # Settings tab -> Client ID
    OIDC_SECRET=...                # Credentials tab -> Secret 

### Configuring Bamboo integration

1. Create a "Social" collection in Bamboo.
2. Copy the collection ID from the URL in Bamboo to pandas-social's `BAMBOO_COLLECTION_ID` environment variable.
3. Create a "PANDAS Social" crawl series in Bamboo.
4. Copy the crawl series ID from the URL in Bamboo to pandas-social's `BAMBOO_CRAWL_SERIES_ID` environment variable.
5. Edit the crawl series and add it to "Social" collection.

Example environment variables:

    BAMBOO_URL=https://bamboo.example.org/bamboo
    BAMBOO_COLLECTION_ID=...
    BAMBOO_CRAWL_SERIES_ID=...