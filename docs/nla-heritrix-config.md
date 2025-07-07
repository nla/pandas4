# NLA Heritrix Configuration

This file documents Heritrix settings used in PANDAS or other NLA crawls where we differ from the defaults.

## Robots policy

We ignore `nofollow` in the `robots` `<meta>` tag, as we've had site owners complain we didn't
archive their site properly and found they were inadvertently using this.

```xml
<bean id="metadata" class="org.archive.modules.CrawlMetadata" autowire="byName">
    <property name="robotsPolicyName" value="robotsTxtOnly"/>
</bean>
```

We do find it helpful to obey `rel=nofollow` on `<a>` links though. In particular Drupal sites that have a search or
browse page with filters.

```xml
 <bean id="extractorHtml" class="org.archive.modules.extractor.ExtractorHTML">
   <property name="obeyRelNofollow" value="true" /> 
 </bean>
```

## Ignoring robots.txt for embeds

It can be helpful to ignore robots.txt for embeds while still following it for navlinks to avoid crawler traps. 
It's not uncommon for a site to block all their JavaScript, stylesheets or images in robots.txt. Presumably this is
because they wrote their robots.txt for search engines and assume search engines only need the HTML pages.

```xml
<!-- Ignore robots.txt for embedded or inferred links (or redirects therefrom) -->
<bean class='org.archive.crawler.spring.DecideRuledSheetAssociation'>
    <property name="targetSheetNames">
        <list>
            <value>ignoreRobotsSheet</value>
        </list>
    </property>
    <property name="rules">
        <bean class="org.archive.modules.deciderules.HopsPathMatchesRegexDecideRule">
            <property name="regex" value=".*[EI]R*"/>
        </bean>
    </property>
</bean>
<bean id='ignoreRobotsSheet' class='org.archive.spring.Sheet'>
    <property name='map'>
        <map>
            <entry key='metadata.robotsPolicyName' value='ignore'/>
        </map>
    </property>
</bean>
```


## Increase WARC pool size

For larger crawls, compressing the WARC records can be a bottleneck, so increasing the WARC writer pool size allows
multiple records to be written in parallel.

```xml
<bean id="warcWriter" class="org.archive.modules.writer.WARCWriterChainProcessor">
  <property name="poolMaxActive" value="16"/>
</bean>
```

## Enable recheckScope

For manually operated crawls, we may add SURT or regex exclusions partway through the crawl has already started. To
ensure these apply to URLs that have already been queued, we need to tell Preselector to recheck the scope before
fetching.

```xml
<bean id="preselector" class="org.archive.crawler.prefetch.Preselector">
  <property name="recheckScope" value="true"/>
</bean>
```


## Reject speculative extracted links

We found speculative extracted links seem to cause more trouble than they're worth, so we block them.

**Note:** Rejecting them in the scope like this is not ideal. If the URL is also later found via non-speculative 
extracted link it will be ignored.

```xml
<bean id="scope" class="org.archive.modules.deciderules.DecideRuleSequence">
    <property name="rules">
        <list>
            ...
            <!-- ...and always REJECT all speculative fetches -->
            <bean id="rejectSpeculative" class="org.archive.modules.deciderules.HopsPathMatchesRegexDecideRule">
                <property name="regex" value=".*X.*"/>
                <property name="decision" value="REJECT"/>
            </bean>
        </list>
    </property>
</bean>
```

## Wordpress rejects

These reject some unnecessary URLs often encountered on Wordpress sites.

```xml
<bean id="scope" class="org.archive.modules.deciderules.DecideRuleSequence">
    <property name="rules">
        <list>
            ...
            <!-- ...and REJECT those from a configurable (initially empty) set of URI regexes... -->
            <bean class="org.archive.modules.deciderules.MatchesListRegexDecideRule">
                <property name="decision" value="REJECT"/>
                <property name="regexList">
                    <list>
                        <!-- Wordpress comment reply forms -->
                        <value>.*\?(?:.*&amp;)?replytocom=\d+</value>
                        <!-- Wordpress sites often redirect to the homepage which in turn pulls in a lot
                             of resources we don't want -->
                        <value>https?://wordpress\.(?:com|org)/</value>
                        <!-- Avoid Wordpress admin pages -->
                        <value>https?://[^/]+/wp-admin/.*</value>
                    </list>
                </property>
            </bean>
            ...
        </list>
    </property>
</bean>
```

## Wix image extractor

Wix has an image server that dynamically scales images, sometimes based on the size of the browser window. This rule
captures the original full-resolution image and not just a downscaled placeholder.

```xml
<bean id="extractorWixStatic" class="org.archive.modules.extractor.ExtractorImpliedURI">
    <property name="regex" value="(https?://static\.wixstatic\.com/media/[/]+)/v1/.*" />
    <property name="format" value="$1" />
</bean>
```

## Allow documents outside SURT scope

It's not uncommon for websites to link a PDF or office document on a different, for example a blog that links to a PDF
file hosted in an S3 bucket. This rule adds to scope links to URLs that have a common document file extension. It also
allows query strings in case they contain an access code.

This rule should go just before `tooManyHopsDecideRule`.

```xml

<bean id="scope" class="org.archive.modules.deciderules.DecideRuleSequence">
    <property name="rules">
        <list>
            ...
            <!-- NLA: also ACCEPT files with common document file extensions even if otherwise outside scope -->
            <bean class="org.archive.modules.deciderules.MatchesRegexDecideRule">
                <property name="decision" value="ACCEPT"/>
                <property name="regex" value="(?i)^[^?]*\.(?:abw|doc|docx|epub|ods|odt|pdf|ppt|pptx|rtf|vsd|xls|xlsx)(?:$|\?.*|;.*)"/>
            </bean>
            <ref bean="tooManyHopsDecideRule" />
            ...
        </list>
    </property>
</bean>
```

## Allow subdomains of SURTs

We turn this on selectively for some sites to pickup their subdomains too.

```xml
<bean id="onDomainsDecideRule" class="org.archive.modules.deciderules.surt.OnDomainsDecideRule">
    <property name="enabled" value="false" />
</bean>
```

## Increase maxTransHops

We increase maxTransHops to 5 from the default of 2 as transitive hops like `iframe → stylesheet → image` aren't uncommon.

```xml
<bean id="transclusionDecideRule" class="org.archive.modules.deciderules.TransclusionDecideRule">
    <property name="maxTransHops" value="5" />
</bean>
```

## Checkpoint regularly and shutdown

We enable `checkpointOnShutdown` so that we can restart the application or server for maintenance and resume the crawls.
We also enable regular 15-minute checkpoints in case of a crash.

```xml
<bean id="checkpointService" class="org.archive.crawler.framework.CheckpointService">
    <property name="checkpointIntervalMinutes" value="15"/>
    <property name="checkpointOnShutdown" value="true"/>
    <property name="forgetAllButLatest" value="true"/>
</bean>
```

## Crawl delay

Trying to balance faster crawler with backing off if the server is responding slowly or requests a slow crawl delay in robots.txt.

```xml
<bean id="disposition" class="org.archive.crawler.postprocessor.DispositionProcessor">
    <property name="delayFactor" value="2.0" />
    <property name="minDelayMs" value="500" />
    <property name="respectCrawlDelayUpToSeconds" value="60" />
    <property name="maxDelayMs" value="30000" />
</bean>
```

## Choice of User-Agent

The most common User-Agent blocking rules we've seen in the wild are:

* blocks if contains `Heritrix`
* blocks if contains `bot` (anywhere)
    - workaround: use the word `archiver` instead of `bot`
* blocks if contains `https://*.` or `www.`
    - workaround: `https:// nla.gov.au/...` (space before hostname)
* blocks if doesn't begin with `Mozilla/5.0`
