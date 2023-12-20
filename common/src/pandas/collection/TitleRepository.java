package pandas.collection;

import org.apache.commons.io.FileUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pandas.agency.Agency;
import pandas.agency.User;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

@Repository
public interface TitleRepository extends CrudRepository<Title,Long> {
    @Query("select t from Title t where t.thumbnails is empty " +
            "and (t.titleUrl is not null or t.seedUrl is not null)")
    List<Title> findWithoutThumbnails(Pageable pageable);

    List<Title> findFirst20ByOwnerOrderByRegDateDesc(User owner);
    List<Title> findFirst20ByAgencyOrderByRegDateDesc(Agency agency);

    long countByOwner(User owner);

    long countByAgency(Agency agency);

    List<Title> findFirst100ByLastModifiedDateAfterOrderByLastModifiedDate(Instant start);

    @Query("select t from Title t where t.gather.method.name = 'Bulk'")
//    @Query("select t from Title t where t.gather.method.name = 'Bulk' and t.gather.nextGatherDate < ?1")
    List<Title> findBulkTitles(Instant now);


    @Query("select t from Title t where t.gather.method.name = ?1 and t.gather.nextGatherDate < ?2 and " +
            "(t.gather.lastGatherDate is null or t.gather.lastGatherDate < ?3)")
    List<Title> fetchNewGathers(String gatherMethod, Instant now, Instant startOfThisMinute);

    String PUBLISH_CONDITIONS = "t.agency.id <> 3\n" +
            "and t.status.name not in ('nominated', 'monitored', 'rejected')\n" +
            "and (t.legalDeposit = true or t.permission.stateName not in ('Denied', 'Unknown'))\n";

    String SUBJECT_CONDITIONS = PUBLISH_CONDITIONS + "AND t.tep.doSubject = true\n";

    String NEW_TITLE_BRIEF = "new pandas.collection.TitleBrief(t.id, t.pi, coalesce(t.tep.displayTitle, t.name), t.agency)";

    @Query("select " + NEW_TITLE_BRIEF + " from Title t\n" +
           "join t.subjects s\n" +
           "where s = :subject\n" +
           "and (:agency is null or t.agency = :agency)\n" +
           "and " + SUBJECT_CONDITIONS +
           "order by coalesce(t.tep.displayTitle, t.name)")
    Page<TitleBrief> findPublishedTitlesInSubject(@Param("subject") Subject subject, @Param("agency") Agency agency, Pageable pageable);

    @Query("select t from Collection c\n" +
            "join c.titles t\n" +
            "where c = :collection\n" +
            "and " + SUBJECT_CONDITIONS +
            "order by coalesce(t.tep.displayTitle, t.name)")
    List<Title> findPublishedTitlesInCollection(@Param("collection") Collection collection);

    @Query("select " + NEW_TITLE_BRIEF + " from Title t\n" +
            "where upper(coalesce(t.tep.displayTitle, t.name)) like :pattern\n" +
            "and (t.agency = :agency or :agency is null)\n" +
            "and " + SUBJECT_CONDITIONS +
            "order by coalesce(t.tep.displayTitle, t.name)")
    Page<TitleBrief> findDisplayableTitlesNamedLike(@Param("pattern") String pattern, @Param("agency") Agency agency, Pageable page);

    @Query("select " + NEW_TITLE_BRIEF + " from Title t\n" +
            "where coalesce(t.tep.displayTitle, t.name) < 'A'\n" +
            "and (t.agency = :agency or :agency is null)\n" +
            "and " + SUBJECT_CONDITIONS +
            "order by coalesce(t.tep.displayTitle, t.name)")
    Page<TitleBrief> findDisplayableTitlesWithNumberNames(@Param("agency") Agency agency, Pageable page);


    List<Title> findByStatusId(long statusId);

    @Query("select t\n" +
            "from Title t\n" +
            "where (:agencyId is null or t.agency.id = :agencyId)\n" +
            "and (:ownerId is null or t.owner.id = :ownerId)\n" +
            "and t.status.name = 'nominated'\n" +
            "and t.awaitingConfirmation = false\n" +
            "order by t.regDate desc")
    Page<Title> worktrayNominated(@Param("agencyId") Long agencyId, @Param("ownerId") Long ownerId, Pageable pageable);

    @Query("select t from Title t\n" +
            "where (:agencyId is null or t.agency.id = :agencyId)\n" +
            "and (:ownerId is null or t.owner.id = :ownerId)\n" +
            "and t.status.name = 'monitored'\n" +
            "and t.awaitingConfirmation = false\n" +
            " order by t.regDate desc")
    Page<Title> worktrayMonitored(@Param("agencyId") Long agencyId, @Param("ownerId") Long ownerId, Pageable pageable);

    @Query("select t from Title t\n" +
            "where (:agencyId is null or t.agency.id = :agencyId)\n" +
            "and (:ownerId is null or t.owner.id = :ownerId)\n" +
            "and t.status.name = 'selected'\n" +
            "and t.awaitingConfirmation = false\n" +
            "and t.permission.stateName = 'Unknown'\n" +
            "and t.legalDeposit = false\n" +
            "order by t.name asc")
    Page<Title> worktrayPermissionRequesting(@Param("agencyId") Long agencyId, @Param("ownerId") Long ownerId, Pageable pageable);

    @Query("select t from Title t\n" +
            "where (:agencyId is null or t.agency.id = :agencyId)\n" +
            "and (:ownerId is null or t.owner.id = :ownerId)\n" +
            "and t.status.name = 'permission requested'\n" +
            "and t.legalDeposit = false\n" +
            " order by t.regDate desc")
    Page<Title> worktrayPermissionRequested(@Param("agencyId") Long agencyId, @Param("ownerId") Long ownerId, Pageable pageable);

    @Query("select t from Title t\n" +
            "where (t.status.name = 'permission granted'\n" +
            "       or (t.legalDeposit = true and (t.status.name in ('selected', 'permission requested', 'permission denied', 'permission impossible'))))\n" +
            "and t.permission.state.name = 'Unknown'\n" +
            "and t.gather.nextGatherDate is null\n" +
            "and t.unableToArchive = false\n" +
            "and t.disappeared = false\n" +
            "and t.awaitingConfirmation = false\n" +
            "and (:agencyId is null or t.agency.id = :agencyId)\n" +
            "and (:ownerId is null or t.owner.id = :ownerId)\n" +
            "order by t.name")
    Page<Title> worktrayAwaitingScheduling(@Param("agencyId") Long agencyId,
                                           @Param("ownerId") Long ownerId,
                                           Pageable pageable);

    @Query("select t from Title t\n" +
            "where t.gather.nextGatherDate is not null\n" +
            "and t.gather.nextGatherDate < :endDate\n" +
            "and t.awaitingConfirmation = false\n" +
            "and (:agencyId is null or t.agency.id = :agencyId)\n" +
            "and (:ownerId is null or t.owner.id = :ownerId)\n" +
            "order by t.gather.nextGatherDate")
    Page<Title> worktrayScheduled(@Param("agencyId") Long agencyId, @Param("ownerId") Long ownerId,
                                  @Param("endDate") Instant endDate, Pageable pageable);

    @Query("select t from Title t\n" +
            "where exists (select i.id from Instance i where i.title = t and i.state.name = 'archived' and i.isDisplayed is null)\n" +
            "and t.awaitingConfirmation = false\n" +
            "and (:agencyId is null or t.agency.id = :agencyId)\n" +
            "and (:ownerId is null or t.owner.id = :ownerId)\n" +
            "order by t.gather.lastGatherDate")
    Page<Title> worktrayArchivedTitles(@Param("agencyId") Long agencyId, @Param("ownerId") Long ownerId, Pageable pageable);

    @Query("select t from Title t\n" +
            "where exists (select i.id from Instance i where i.title = t and i.state.name <> 'deleted')\n" +
            "and t.anbdNumber is null\n" +
            "and t.awaitingConfirmation = false\n" +
            "and (t.legalDeposit = true or t.status.name not in ('permission impossible', 'permission denied'))\n" +
            "and t.cataloguingNotRequired <> true\n" +
            "and (:agencyId is null or t.agency.id = :agencyId)\n" +
            "and (:ownerId is null or t.owner.id = :ownerId)\n" +
            "order by t.gather.lastGatherDate")
    Page<Title> worktrayAwaitingCataloguing(@Param("agencyId") Long agencyId, @Param("ownerId") Long ownerId, Pageable pageable);

    Optional<Title> findByPi(long pi);

    List<Title> findByTitleUrlIn(List<String> urls);

    @Query("select distinct t from Title t\n" +
            " join t.statusHistories sh\n" +
            " where sh.user = :nominator and " +
            "       sh.status.name in ('nominated', 'selected')\n and " +
            "       sh.startDate > :dateLimit " +
            " order by t.regDate desc")
    List<Title> findByNominatorOrSelector(@Param("nominator") User nominator, @Param("dateLimit") Instant dateLimit);

    @Query("select t from Title t\n" +
            " join t.statusHistories sh\n" +
            " where sh.user = :selector and " +
            "       sh.status.name = 'selected'\n and " +
            "       sh.startDate > :dateLimit " +
            " order by t.regDate desc")
    List<Title> findBySelector(@Param("selector") User selector, @Param("dateLimit") Instant dateLimit);

    @Query("select count(*) from Issue i where i.group.tep.title = :title")
    long countIssues(@Param("title") Title title);

    @Query("select max(title.pi) from Title title")
    Long maxPi();

    @Query("""
            select title from Title title
             where title.id in (select instance.title.id from Instance instance where instance.tepUrl = :url)
                   or title.id in (select issue.instance.title.id from Issue issue where issue.url = :url)
            """)
    List<Title> findByUrl(@Param("url") String url);

    Stream<Title> findBySeedUrlLike(String seedUrlPattern);

    interface TitleListItem {
        long getId();
        String getName();
    }

    @Query("""
            select t.id as id, t.name as name
            from Title t
            join t.subjects s
            where :subject = s
            order by t.name
            """)
    List<TitleListItem> listBySubject(Subject subject);

    @Query("""
            select s.id as id,
                s.name as name,
                count(t) as count
            from Subject s
            join s.titles t
            where t.regDate >= :start and t.regDate <= :end
            group by s.id, s.name
            order by count(t) desc
            """)
    List<SubjectCount> countBySubject(@Param("start") Instant start, @Param("end") Instant end);

    long countByRegDateBetween(Instant start, Instant end);

    interface SubjectCount {
        Long getId();
        String getName();
        long getCount();

        default List<Object> values() {
            return List.of(getCount());
        }

        default String key() {
            return getName();
        }

        default String link() {
            return "../subjects/" + getId();
        }
    }
}