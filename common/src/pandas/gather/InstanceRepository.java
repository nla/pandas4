package pandas.gather;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pandas.agency.Agency;
import pandas.agency.User;
import pandas.collection.Collection;
import pandas.collection.Title;

import java.util.List;

@Repository
public interface InstanceRepository extends CrudRepository<Instance,Long> {
    @Query("select i from Instance i where i.state.name in ('gathering', 'gatherPause', 'gatherProcess', " +
            "'archiving', 'deleting') order by i.date")
    List<Instance> findGathering();

    @Query("select i from Instance i where i.state.name = 'failed' order by i.date")
    List<Instance> findFailed();

    @Query("select i from Instance i where i.state.id in (12, 9, 7, 13) and i.gatherMethodName = ?1")
    List<Instance> findIncomplete(String gatherMethodName);

    @Query("select i.state.name from Instance i where i.id = ?1")
    String getStateName(long instanceId);

    @Modifying
    @Query("update Instance i set i.state = (select s from State s where s.name = ?2) where i.id = ?1")
    void updateState(Long id, String stateName);

    @Query("select i from Instance i where not exists (select it from InstanceThumbnail it where it.instanceId = i.id) " +
            "and i.state.name = 'archived'")
    List<Instance> findWithoutThumbnails(Pageable pageable);

    List<Instance> findByTitle(Title title);

    @Query("select i from Instance i where i.title = :title and i.state.name <> 'deleted' order by i.date desc")
    List<Instance> findRecentGathers(@Param("title") Title title, Pageable pageable);

    @Query("select i from Instance i where i.title = :title and i.state.name = 'archived' and i.isDisplayed = 1 order by i.date desc")
    List<Instance> findDisplayedByTitle(@Param("title") Title title);

    @Query("select i from Instance i\n" +
            "where i.state.name in ('gathering', 'gatherPause', 'gatherProcess')\n" +
            "and i.title.awaitingConfirmation = false\n" +
            "and (:agencyId is null or i.title.agency.id = :agencyId)\n" +
            "and (:ownerId is null or i.title.owner.id = :ownerId)\n" +
            "order by i.date desc")
    Page<Instance> worktrayGathering(@Param("agencyId") Long agencyId, @Param("ownerId") Long ownerId, Pageable pageable);

    @Query("select i from Instance i\n" +
            "where i.state.name = 'creation'\n" +
            "and i.gatherMethodName = 'Upload'\n" +
            "and i.title.awaitingConfirmation = false\n" +
            "and (:agencyId is null or i.title.agency.id = :agencyId)\n" +
            "and (:ownerId is null or i.title.owner.id = :ownerId)\n" +
            "order by i.date desc")
    Page<Instance> worktrayInstancesForUpload(@Param("agencyId") Long agencyId, @Param("ownerId") Long ownerId, Pageable pageable);

    @Query("select count(*) from Instance i\n" +
            "where i.state.name = 'gathered'\n" +
            "and i.title.awaitingConfirmation = false\n" +
            "and i.title.owner.id = :ownerId")
    long countGatheredWorktray(@Param("ownerId") Long ownerId);

    @Query("select i from Instance i\n" +
            "where i.state.name = 'gathered'\n" +
            "and i.title.awaitingConfirmation = false\n" +
            "and (:agencyId is null or i.title.agency.id = :agencyId)\n" +
            "and (:ownerId is null or i.title.owner.id = :ownerId)\n" +
            "order by i.id desc")
    Page<Instance> listGatheredWorktray(@Param("agencyId") Long agencyId, @Param("ownerId") Long ownerId, Pageable pageable);

    @Query("select i from Instance i\n" +
            "where i.state.name = 'gathered'\n" +
            "and i.title.awaitingConfirmation = false\n" +
            "and (:agency is null or i.title.agency = :agency)\n" +
            "and (:owner is null or i.title.owner = :owner)\n" +
            "and i.id < :instanceId\n" +
            "order by i.id desc")
    Page<Instance> nextInGatheredWorktray(@Param("agency") Agency agency, @Param("owner") User owner,
                                              @Param("instanceId") long instanceId, Pageable pageable);

    @Query("select i from Instance i\n" +
            "where i.state.name = 'gathered'\n" +
            "and i.title.awaitingConfirmation = false\n" +
            "and (:agency is null or i.title.agency = :agency)\n" +
            "and (:owner is null or i.title.owner = :owner)\n" +
            "and i.id > :instanceId\n" +
            "order by i.id asc")
    Page<Instance> prevInGatheredWorktray(@Param("agency") Agency agency, @Param("owner") User owner,
                                              @Param("instanceId") long instanceId, Pageable pageable);

    @Query("select new pandas.gather.PreviousGather(cur.id, prev.id, prev.date, prev.gather.files, prev.gather.size) " +
            "from Instance cur " +
            "inner join Instance prev " +
            "   on prev.id = (select max(i.id) from Instance i " +
            "                 where i.title = cur.title " +
            "                   and i.id < cur.id " +
            "                   and i.state.name = 'archived') " +
            "where cur in (:instances)")
    List<PreviousGather> findPreviousStats(@Param("instances") List<Instance> instances);

    @Query("""
        select instance
        from Instance instance
        join instance.title title
        join title.collections col
        where col = :collection
        and instance.id = (select min(i.id) from Instance i
                    where i.title.id = title.id 
                    and i.state.id = 1
                    and (col.startDate is null or i.date >= col.startDate)
                    and (col.endDate is null or i.date <= col.endDate)) 
        order by title.name 
        """)
    List<Instance> findByCollection(@Param("collection") Collection collection);
}
