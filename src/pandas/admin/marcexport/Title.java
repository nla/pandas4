package pandas.admin.marcexport;

import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import java.util.Date;

public interface Title {
    long getId();

    String getName();

    String getTitleUrl();

    Date getRegisteredDate();

    String getPublisherName();

    String getPublisherUrl();

    int getPublisherTypeId();

    default PublisherType getPublisherType() {
        return PublisherType.byId(getPublisherTypeId());
    }

    int getFormatId();

    default Format getFormat() {
        return Format.byId(getFormatId());
    }

    Date getEarliestInstanceDate();
}
