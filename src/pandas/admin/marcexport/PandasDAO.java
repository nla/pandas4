package pandas.admin.marcexport;

import org.skife.jdbi.v2.sqlobject.Bind;
import org.skife.jdbi.v2.sqlobject.SqlQuery;
import org.skife.jdbi.v2.sqlobject.helpers.MapResultAsBean;

import java.util.Date;
import java.util.List;

public interface PandasDAO {

    @SqlQuery("select " +
            "TITLE.PI id, " +
            "TITLE.NAME name, " +
            "TITLE.TITLE_URL titleUrl, " +
            "TITLE.REG_DATE registeredDate, " +
            "(SELECT MIN(INSTANCE_DATE) FROM INSTANCE WHERE INSTANCE.TITLE_ID = TITLE.TITLE_ID) earliestInstanceDate, " +
            "TITLE.FORMAT_ID formatId, " +
            "ORGANISATION.NAME publisherName, " +
            "ORGANISATION.URL publisherUrl, " +
            "PUBLISHER.PUBLISHER_TYPE_ID publisherTypeId " +
            "from TITLE " +
            "left join PUBLISHER on PUBLISHER.PUBLISHER_ID = TITLE.PUBLISHER_ID " +
            "left join ORGANISATION on ORGANISATION.PUBLISHER_ID = TITLE.PUBLISHER_ID " +
            "where TITLE.PUBLISHER_ID = ORGANISATION.PUBLISHER_ID and TITLE.PI = :pi")
    @MapResultAsBean
    Title findTitle(@Bind("pi") long pi);

    @SqlQuery("SELECT " +
            "  t.PI pi, " +
            "  t.NAME name, " +
            "  f.NAME format, " +
            "  own.USERID owner, " +
            "  i.INSTANCE_DATE instanceDate,\n" +
            "  sh.START_DATE archivedDate,\n" +
            "  t.AGENCY_ID agencyId\n" +
            "FROM STATE_HISTORY sh, INSTANCE i, TITLE t, INDIVIDUAL own, FORMAT f\n" +
            "WHERE\n" +
            "  sh.INSTANCE_ID = i.INSTANCE_ID AND\n" +
            "  sh.STATE_ID = 1 AND\n" +
            "  sh.START_DATE >= :startDate AND sh.START_DATE < :endDate AND\n" +
            "  i.INSTANCE_ID = (SELECT MIN(i2.INSTANCE_ID) FROM INSTANCE i2 WHERE i2.TITLE_ID = i.TITLE_ID AND i2.CURRENT_STATE_ID = 1) AND\n" +
            "  t.TITLE_ID = i.TITLE_ID AND\n" +
            "  f.FORMAT_ID = t.FORMAT_ID AND\n" +
            "  own.INDIVIDUAL_ID = t.CURRENT_OWNER_ID AND\n" +
            "\n" +
            "  ((:includeMono = 1 AND f.NAME = 'Mono') OR\n" +
            "   (:includeIntegrating = 1 AND f.name = 'Integrating') OR\n" +
            "   (:includeSerial = 1 AND f.name = 'Serial')) AND\n" +
            "\n" +
            "  (:includeCataloguingNotRequired = 1 OR (t.IS_CATALOGUING_NOT_REQ IS NULL OR t.IS_CATALOGUING_NOT_REQ = 0)) AND\n" +
            "  (:includeCollectionMembers = (CASE WHEN EXISTS(SELECT 1 FROM TITLE_COL tc WHERE tc.TITLE_ID = t.TITLE_ID) THEN 1 ELSE 0 END))"
    )
    @MapResultAsBean
    List<TitleSummaryRow> listTitles(@Bind("startDate") Date startDate,
                                     @Bind("endDate") Date endDate,
                                     @Bind("includeMono") boolean includeMono,
                                     @Bind("includeIntegrating") boolean includeIntegrating,
                                     @Bind("includeSerial") boolean includeSerial,
                                     @Bind("includeCataloguingNotRequired") boolean includeCataloguingNotRequired,
                                     @Bind("includeCollectionMembers") boolean includeCollectionMembers);

}
