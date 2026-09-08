package pandas.core;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class LinkProperties {
    private final String deliveryBaseUrl;
    private final String collectionIdentifierBaseUrl;
    private final String titleIdentifierBaseUrl;
    private final String webdavBaseUrl;
    private final String ftpBaseUrl;
    private final String bambooBaseUrl;

    public LinkProperties(
            Config config,
            @Value("${pandas.deliveryBaseUrl:https://webarchive.nla.gov.au/awa/}") String deliveryBaseUrl,
            @Value("${pandas.collectionIdentifierBaseUrl:https://nla.gov.au/nla.arc-c}") String collectionIdentifierBaseUrl,
            @Value("${pandas.titleIdentifierBaseUrl:https://nla.gov.au/nla.arc-}") String titleIdentifierBaseUrl,
            @Value("${pandas.webdavBaseUrl:https://pandas.nla.gov.au/dav/}") String webdavBaseUrl,
            @Value("${pandas.ftpBaseUrl:ftp://pandas-ftp.nla.gov.au/working/}") String ftpBaseUrl) {
        this.deliveryBaseUrl = deliveryBaseUrl;
        this.collectionIdentifierBaseUrl = collectionIdentifierBaseUrl;
        this.titleIdentifierBaseUrl = titleIdentifierBaseUrl;
        this.webdavBaseUrl = webdavBaseUrl;
        this.ftpBaseUrl = ftpBaseUrl;
        this.bambooBaseUrl = config.getBambooUrl().replaceFirst("/+$", "");
    }

    String deliveryBaseUrl() {
        return deliveryBaseUrl;
    }

    String collectionIdentifierBaseUrl() {
        return collectionIdentifierBaseUrl;
    }

    String titleIdentifierBaseUrl() {
        return titleIdentifierBaseUrl;
    }

    String webdavBaseUrl() {
        return webdavBaseUrl;
    }

    String ftpBaseUrl() {
        return ftpBaseUrl;
    }

    String bambooBaseUrl() {
        return bambooBaseUrl;
    }
}
