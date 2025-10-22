# PANDAS Changelog

## Unreleased

## 4.10.0 2025-10-22

### New features

- Gather indicators (gather quality metrics) are now computed and stored at gather time: [#68](https://github.com/nla/pandas4/pull/68) 
  - proportion of HTTP 2XX, 403, 5XX responses
  - file size change / decrease against archived
  - thumbnail image diff (live/archived) (perceptual hashing)
- QA worktray filters for success and problem indicators.
- QA screen displays the gather indicators plus two 'overall' metrics:
  - 'gather' based on just this gather 
  - 'archived' including comparison to last archived gather

### Bug fixes

- Added a timeout when waiting for Heritrix job to finish stopping before teardown

### Dependency upgrades

- **agrona**: 2.2.4 → 2.3.0
- **commons-compress**: 1.28.0 → removed
- **guava**: 33.4.8-jre → 33.5.0-jre
- **postgresql**: 42.7.7 → 42.7.8
- **slim-select**: 2.10.0 → 2.12.1
- **spring-boot**: 3.5.5 → 3.5.6
- **webshim**: 1.15.8 → 1.15.8-1

## 4.9.1 2025-09-04

### Bug fixes

* Fix **Parent collection** not populating on the edit collection screen.
* Fix UnsupportedOperationException on bulk change screen.

## 4.9.0 2025-09-03

### New features

* Added settings for thumbnail generation browser (`executable`, `options`, `limit`).
* App startup banner now shows git version info to help identify the build when running on Kubernetes.
* Exit status for gatherer processes is now recorded.

### Bug fixes

* Fixed Google bookmarklet text appearing upside down.
* Fixed exception populating state and status tables on MySQL.

### Changes

* Removed TypeScript from the build. Switched to JSDoc for type annotations.

### Dependency upgrades

- **jsoup**: 1.21.1 → 1.21.2
- **spring-boot**: 3.5.4 → 3.5.5
- **zero-allocation-hashing**: 0.27ea0 → 0.27ea1

## 4.8.0

### New features

* Contact events can now be added to titles and publishers.
* WebDAV and FTP working area URLs are now displayed on the view instance screen.
* Implemented most of worktray actions that were a "TODO" placeholder.
* New title and publisher contact people can now be created inline from the title, permission and contact event forms.
* 'Titles archived by agency' report under Statistics. This is the 'Total archived titles' report from PANDAS 3.
* MySQL database support.

### Bug fixes

* Fixed broken link on view title screen to the contact person for a contact event when the person is a publisher contact.
* Fixed a null pointer exception on the issues editor when a title has no instances.
* Fixed exception deleting title contact people.

### Changes

* Webjar version numbers are no longer hard-coded in templates and are now resolved by webjars-locator-lite.

### Dependency upgrades

- **browsertrix**: 1.6.0 → 1.7.0
- **commons-compress**: 1.27.1 → 1.28.0
- **commons-csv**: 1.14.0 → 1.14.1
- **commons-io**: 2.19.0 → 2.20.0
- **p6spy-spring-boot-starter**: 1.11.0 → 1.12.0
- **slim-select**: 2.5.1 → 2.10.0
- **spring-boot**: 3.5.3 → 3.5.4

## 4.7.1

### Bug fixes

- Fixed title owners missing permission to retry failed instances.
- The gatherer now retries GET requests to the Heritrix API that returned an empty body.

### Dependency upgrades

- **jwarc**: 0.31.1 → 0.32.0

## 4.7.0

### New features

- Gather profiles can now include custom Browsertrix command-line options.

## 4.6.2

### Bug fixes

- Fixed an exception changing user access levels with newer versions of Keycloak.

## 4.6.1

### Bug fixes

- **Issue editor:** Fixed some more corner cases.

## 4.6.0

### New features

- **Issue editor prefill:** The issue URL is now prefilled with the TEP URL of the selected instance.

### Bug fixes

- **Issue editor error:** Fixed error when moving an existing issue to a newly created issue group.

## 4.5.4

### Bug fixes

- **Bulk add websites:** Fixed broken 'Gather immediately' checkbox.

### Dependency updates

- **jsoup:** 1.19.1 → 1.20.1

## 4.5.3

### Bug fixes

- **Browsertrix exit codes:** Don't treat the new size and time exit codes as a failed crawl.

### Dependency updates

- **browsertrix-crawler:** 1.5.8 → 1.6.0
- **commons-io:** 2.18.0 → 2.19.0
- **guava:** 33.4.0-jre → 33.4.8-jre
- **p6spy-spring-boot-starter:** 1.10.0 → 1.11.0
- **spring-boot:** 3.4.4 → 3.4.5

## 4.5.2

### Bug fixes

- **Pages tool sort order:** Snapshots now sorted by date descending rather than URL.
- **Pages tool crawl links:** Fixed typo in link to Bamboo crawls.

## 4.5.1

### Bug fixes

- **Custom behavior support for Bluesky crawling:** Added a behavior script to ensure all of Bluesky's language-specific
  JavaScript chunks are archived, fixing replay when the browser has a different primary language to the crawler.

## 4.5.0

### New features

- **Collection stats:** The date range and number of publishers of archived titles in the collection is now shown.   
  Clicking the publishers count will show the list of publishers ordered by number of titles. This is intended to aid 
  cataloguing of collections.

## 4.4.0

### New features

- **Agency transfer contacts:** You can now designate one user per agency as the transfer contact on the Edit Agency 
  screen. When initiating a title transfer, selecting an agency will automatically pre‑select that agency’s transfer
  contact.

## 4.3.0

### New features

- **Experimental QA replay:** Added a preview of the pywb client-side replay mode to the QA screen.

### Dependency updates

- **agrona:** 2.0.1 → 2.1.0
- **browsertrix-crawler:** 1.4.1 → 1.5.8
- **commons-csv:** 1.13.0 → 1.14.0
- **guava:** 33.4.0-jre → 33.4.5-jre
- **hibernate-search:** 7.2.2 → 7.2.3
- **jsoup:** 1.18.3 → 1.19.1
- **okhttp:** removed
- **spring-boot:** 3.4.3 → 3.4.4

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