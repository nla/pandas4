PANDAS Gatherer
===============

The PANDAS Gatherer application manages the execution of crawl jobs and other tasks such as archiving.

Running It
----------

The gatherer builds as self-contained jar which can be run using:

    java -Xmx1g -jar panads4-gatherer.jar

The [HTTrack](https://www.httrack.com/) and [Heritrix](https://github.com/internetarchive/heritrix3) crawlers need to
be installed. The HTTrack backend also assumes tar and md5sum (coreutils) are available.

Configuration is specified through environment variables:

    PANDAS_DB_URL=
    PANDAS_DB_USER=
    PANDAS_DB_PASSWORD=

    PANDAS_HOME=data

    HERITRIX_WORKERS=8
    HERITRIX_URL=https://localhost:8443/engine
    HERITRIX_USER=
    HERITRIX_PASSWORD=

    HTTRACK_WORKERS=20
    HTTRACK_EXECUTABLE=/usr/bin/httrack

Gather Methods
--------------

### Browsertrix setup

PANDAS runs the browsertrix-crawler container using podman in rootless mode. This needs a little setup:
~~~~
    yum install -y podman
    echo pandas:200000:65536 >> /etc/subuid
    echo pandas:200000:65536 >> /etc/subgid
    sudo -u pandas podman system migrate
    sudo -u pandas podman pull webrecorder/browsertrix-crawler

Architecture
------------
~~~~There are separate backends for each of the [HTTrack](src/pandas/gatherer/httrack),
[Heritrix](src/pandas/gatherer/heritrix) and [upload](src/pandas/gatherer/httrack/UploadGatherer.java) crawl methods
plus a specialised ["scripter"](src/pandas/gatherer/scripter) module for other tasks like find and replace. Each
backend has an associated thread pool. Worker threads poll the database for new jobs to do. The 
[GatherManager](src/pandas/gatherer/core/GatherManager.java) maintains a list of actively processing titles which
ensures that only a single job can be run at a time for any given title.

HTTrack crawls are converted to WARC files by the primitive
[Pandora2Warc converter](src/pandas/gatherer/httrack/Pandora2Warc.java). This conversion leaves the URLs using the 
filenames as renamed by HTTrack. In the near future we'll likely to switch to the more sophisticated
[httrack2warc](https://github.com/nla/httrack2warc) converter instead which undoes HTTrack's renaming and produce a 
WARC with original URLs intact.

History
-------
The PANDAS 4 gatherer is based on the PANDAS 3 codebase which used a different data access layer (EnterpriseObjects in
2007, later ported to JDBI in 2018). The P3 gatherer called a suite of Perl scripts for any operations involving
manipulating files, these were translated to Java as part of importing the project into the PANDAS 4 repository in 2021. 