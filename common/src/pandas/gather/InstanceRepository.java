package pandas.gather;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pandas.agency.Agency;
import pandas.agency.User;
import pandas.collection.Collection;
import pandas.collection.Title;

import java.time.Instant;
import java.util.List;

@Repository
public interface InstanceRepository extends CrudRepository<Instance,Long>, JpaSpecificationExecutor<Instance> {
    default Instance getOrThrow(long id) {
        return findById(id).orElseThrow(() -> new EntityNotFoundException("Instance not found: " + id));
    }

    List<Instance> findByStateInOrderByDate(List<State> states);

    @Query("""
            select i from Instance i
            where i.state in (
              pandas.gather.State.GATHERING,
              pandas.gather.State.GATHER_PROCESS,
              pandas.gather.State.DELETING,
              pandas.gather.State.ARCHIVING)
            and i.gatherMethodName = ?1
            order by case
                when i.state = pandas.gather.State.DELETING then 0
                when i.state = pandas.gather.State.ARCHIVING then 1
                when i.state = pandas.gather.State.GATHER_PROCESS then 2
                when i.state = pandas.gather.State.GATHERING then 3
                else 4
            end, i.date""")
    List<Instance> findIncomplete(String gatherMethodName);

    @Query("select i from Instance i where not exists (select it from InstanceThumbnail it where it.instanceId = i.id) " +
            "and i.state = pandas.gather.State.ARCHIVED")
    List<Instance> findWithoutThumbnails(Pageable pageable);

    List<Instance> findByTitle(Title title);

    @Query("select i from Instance i where i.title = :title and i.state <> pandas.gather.State.DELETED order by i.date desc")
    List<Instance> findRecentGathers(@Param("title") Title title, Pageable pageable);

    @Query("select i from Instance i where i.title = :title and i.state = pandas.gather.State.ARCHIVED and i.isDisplayed = true order by i.date desc")
    List<Instance> findDisplayedByTitle(@Param("title") Title title);

    @Query("""
            select i from Instance i
            where i.state in (
                pandas.gather.State.GATHERING,
                pandas.gather.State.GATHER_PAUSE,
                pandas.gather.State.GATHER_PROCESS)
            and i.title.awaitingConfirmation = false
            and (:agencyId is null or i.title.agency.id = :agencyId)
            and (:ownerId is null or i.title.owner.id = :ownerId)
            order by i.date desc""")
    Page<Instance> worktrayGathering(@Param("agencyId") Long agencyId, @Param("ownerId") Long ownerId, Pageable pageable);

    @Query("select i from Instance i\n" +
            "where i.state = pandas.gather.State.CREATION\n" +
            "and i.gatherMethodName = 'Upload'\n" +
            "and i.title.awaitingConfirmation = false\n" +
            "and (:agencyId is null or i.title.agency.id = :agencyId)\n" +
            "and (:ownerId is null or i.title.owner.id = :ownerId)\n" +
            "order by i.date desc")
    Page<Instance> worktrayInstancesForUpload(@Param("agencyId") Long agencyId, @Param("ownerId") Long ownerId, Pageable pageable);

    @Query("select count(*) from Instance i\n" +
            "where i.state = pandas.gather.State.GATHERED\n" +
            "and i.title.owner.id = :ownerId")
    long countGatheredWorktray(@Param("ownerId") Long ownerId);

    @Query("select count(*) from Instance i\n" +
            "where i.state = pandas.gather.State.GATHERED\n" +
            "and i.title.agency.id = :agencyId")
    long countGatheredWorktrayByAgency(@Param("agencyId") Long agencyId);

    @Query("""
        select i from Instance i
        where i.state = pandas.gather.State.GATHERED
        and (:agencyId is null or i.title.agency.id = :agencyId)
        and (:ownerId is null or i.title.owner.id = :ownerId)
        order by i.id desc""")
    Page<Instance> listGatheredWorktray(Long agencyId, Long ownerId, Pageable pageable);

    @Query("select i from Instance i\n" +
            "where i.state = pandas.gather.State.GATHERED\n" +
            "and (:agency is null or i.title.agency = :agency)\n" +
            "and (:owner is null or i.title.owner = :owner)\n" +
            "and i.id < :instanceId\n" +
            "order by i.id desc")
    Page<Instance> nextInGatheredWorktray(@Param("agency") Agency agency, @Param("owner") User owner,
                                              @Param("instanceId") long instanceId, Pageable pageable);

    @Query("select i from Instance i\n" +
            "where i.state = pandas.gather.State.GATHERED\n" +
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
            "                   and i.state = pandas.gather.State.ARCHIVED) " +
            "where cur in (:instances)")
    List<PreviousGather> findPreviousStats(@Param("instances") List<Instance> instances);

    @Query("""
              select instance
              from Instance instance
              join instance.title title
              join title.collections col
              where col = :collection
              and instance.id = coalesce(
                  (select min(i.id) from Instance i
                          where i.title.id = title.id
                          and i.state = pandas.gather.State.ARCHIVED
                          and (i.isDisplayed is null or i.isDisplayed = true)
                          and i.date >= :time),
                  (select max(i.id) from Instance i
                          where i.title.id = title.id
                          and i.state = pandas.gather.State.ARCHIVED
                          and i.date < :time))
              order by title.name
            """)
    List<Instance> findByCollectionAt(@Param("collection") Collection collection, @Param("time") Instant time);

    @Query("""
            select instance
            from Instance instance
            join instance.title title
                join title.collections col
                where col = :collection
                and instance.id = (select min(i.id) from Instance i
                            where i.title.id = title.id
                            and i.state = pandas.gather.State.ARCHIVED
                            and (i.isDisplayed is null or i.isDisplayed = true)
                            and (col.startDate is null or i.date >= col.startDate)
                            and (col.endDate is null or i.date <= col.endDate)) 
                order by title.name 
                """)
    List<Instance> findByCollection(@Param("collection") Collection collection);

    @Query("select count(i) from Instance i where i.state = pandas.gather.State.ARCHIVED")
    long countArchived();
}
