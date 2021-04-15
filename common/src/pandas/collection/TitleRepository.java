package pandas.collection;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface TitleRepository extends CrudRepository<Title,Long> {
    @Query("select t from Title t where t.thumbnails is empty " +
            "and (t.titleUrl is not null or t.seedUrl is not null)")
    List<Title> findWithoutThumbnails(Pageable pageable);

    List<Title> findFirst100ByLastModifiedDateAfterOrderByLastModifiedDate(Instant start);

    @Query("select t from Title t where t.gather.method.name = 'Bulk' and t.gather.nextGatherDate < ?1")
    List<Title> findBulkTitles(Instant now);


    @Query("select t from Title t where t.gather.method.name = ?1 and t.gather.nextGatherDate < ?2 and " +
            "(t.gather.lastGatherDate is null or t.gather.lastGatherDate < ?3)")
    List<Title> fetchNewGathers(String gatherMethod, Instant now, Instant startOfThisMinute);

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
            "where (t.status.name = 'permission granted'\n" +
            "       or (t.legalDeposit = true and t.status.name in ('selected', 'permission requested',\n" +
            "                                              'permission denied', 'permission impossible')))\n" +
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
}
