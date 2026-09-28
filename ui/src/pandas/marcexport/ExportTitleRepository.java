package pandas.marcexport;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
public interface ExportTitleRepository extends JpaRepository<ExportTitleRepository.Dummy, Long> {
    @Entity
    class Dummy {
        @Id
        long id;
    }

    @Query(value = "select " +
            "title.PI id, " +
            "title.NAME name, " +
            "title.TITLE_URL titleUrl, " +
            "title.REG_DATE registeredDate, " +
            "(SELECT MIN(INSTANCE_DATE) FROM instance WHERE instance.TITLE_ID = title.TITLE_ID) earliestInstanceDate, " +
            "title.FORMAT_ID formatId, " +
            "organisation.NAME publisherName, " +
            "organisation.URL publisherUrl, " +
            "publisher.PUBLISHER_TYPE_ID publisherTypeId " +
            "from title " +
            "left join publisher on publisher.PUBLISHER_ID = title.PUBLISHER_ID " +
            "left join organisation on organisation.ORGANISATION_ID = publisher.ORGANISATION_ID " +
            "where title.PI = :pi", nativeQuery=true)
    Title findTitle(@Param("pi") long pi);

    @Query(value = "SELECT \n" +
            "  t.PI pi, \n" +
            "  t.NAME name, \n" +
            "  f.NAME format, \n" +
            "  own.USERID owner, \n" +
            "  i.INSTANCE_DATE instanceDate,\n" +
            "  sh.START_DATE archivedDate,\n" +
            "  t.AGENCY_ID agencyId\n" +
            "FROM state_history sh, instance i, title t, individual own, format f\n" +
            "WHERE\n" +
            "  sh.INSTANCE_ID = i.INSTANCE_ID AND\n" +
            "  sh.STATE_ID = 1 AND\n" +
            "  sh.START_DATE >= :startDate AND sh.START_DATE < :endDate AND\n" +
            "  i.INSTANCE_ID = (SELECT MIN(i2.INSTANCE_ID) FROM instance i2 WHERE i2.TITLE_ID = i.TITLE_ID AND i2.CURRENT_STATE_ID = 1) AND\n" +
            "  t.TITLE_ID = i.TITLE_ID AND\n" +
            "  t.AGENCY_ID = 1 AND\n" +
            "  f.FORMAT_ID = t.FORMAT_ID AND\n" +
            "  own.INDIVIDUAL_ID = t.CURRENT_OWNER_ID AND\n" +
            "\n" +
            "  ((:includeMono = 1 AND f.NAME = 'Mono') OR\n" +
            "   (:includeIntegrating = 1 AND f.name = 'Integrating') OR\n" +
            "   (:includeSerial = 1 AND f.name = 'Serial')) AND\n" +
            "\n" +
            "  (:includeCataloguingNotRequired = 1 OR (t.IS_CATALOGUING_NOT_REQ IS NULL OR t.IS_CATALOGUING_NOT_REQ = 0)) AND\n" +
            "  (:includeCollectionMembers = (CASE WHEN EXISTS(SELECT 1 FROM title_col tc WHERE tc.TITLE_ID = t.TITLE_ID) THEN 1 ELSE 0 END))"
            , nativeQuery = true)
    List<TitleSummary> listSummaries(@Param("startDate") Date startDate,
                                     @Param("endDate") Date endDate,
                                     @Param("includeMono") boolean includeMono,
                                     @Param("includeIntegrating") boolean includeIntegrating,
                                     @Param("includeSerial") boolean includeSerial,
                                     @Param("includeCataloguingNotRequired") boolean includeCataloguingNotRequired,
                                     @Param("includeCollectionMembers") boolean includeCollectionMembers);

}
