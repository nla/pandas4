# PANDAS Changelog

## 4.2.3

### Bug fixes
- **QA CDX indexes for Browsertrix crawls** are now generated with pywb instead browsertrix's --generatecdx option. It
  seems the Browsertrix indexes are slightly different and pywb is sometimes unable to find pages in them.

## 4.2.2

### Bug fixes
- **Login error messages:**  If an OpenID Connect login attempt throws an exception the message is now shown to the user
  and the backtrace logged. Previously the error was just hidden and the user hit a redirect loop.
- **Duplicate date instances:** PANDAS prevents two instances of the same title to be created within the same minute
  because the instance date (with minute accuracy) is used for the working folder area paths and persistent identifiers.
  This wasn't working correctly as calculating the start of the current minute wasn't clearing the milliseconds part of the time.

## 4.2.1

### Bug fixes
- **Edit issues:** The issues table now has column titles and the 'Add issue' button pre-selects the most recent instance. 

### Dependency updates
- **spring-boot:** 3.4.2 → 3.4.3

## 4.2.0

### New features
- **Keycloak migration tools:** Added tools to dump and import credentials to facilitate retiring the PANDAS
  Keycloak plugin.
- **QA worktray alert:** A blue alert is now shown to make it more obvious when someone else's worktray is shown.

### Dependency updates
- **agrona:** 1.18.1 → 2.0.1
- **commons-compress:** 1.26.0 → 1.27.1
- **commons-csv:** 1.8 → 1.13.0
- **commons-io:** 2.14.0 → 2.18.0
- **failsafe:** 3.3.1 → 3.3.2
- **freelib-marc4j:** 2.6.11 → 2.6.12
- **guava:** 32.0.0-jre → 33.4.0-jre
- **jetbrains-annotations:** 23.0.0 → 26.0.2
- **Java-WebSocket:** 1.5.0 → 1.6.0
- **jsoup:** 1.18.2 → 1.18.3
- **lucene-misc:** 8.7.0 → 9.11.1
- **marc4j:** freelib-marc4j 2.6.12 → org.marc4j 2.9.6
- **nanojson:** 1.6 → 1.9
- **owasp-java-html-sanitizer:** 20211018.1 → 20240325.1
- **p6spy-spring-boot-starter:** 1.8.1 → 1.10.0
- **postgresql:** 42.4.4 → 42.7.5
- **spring-boot:** 3.4.1 → 3.4.2
- **spring-shell-starter:** 2.1.1 → 3.4.0
- **zero-allocation-hashing:** 0.16 → 0.27ea0