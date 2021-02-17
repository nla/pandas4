package pandas.marcexport;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.util.Date;

public interface TitleSummary {
    long getPi();

    String getName();

    String getFormat();

    String getOwner();

    @JsonFormat(pattern="yyyy-MM-dd")
    Date getInstanceDate();

    @JsonFormat(pattern="yyyy-MM-dd")
    Date getArchivedDate();

    int getAgencyId();
}
