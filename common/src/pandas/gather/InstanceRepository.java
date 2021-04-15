package pandas.gather;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pandas.collection.Title;

import java.util.List;

@Repository
public interface InstanceRepository extends CrudRepository<Instance,Long> {
    @Query("select i from Instance i where i.state.name in ('gathering', 'gatherPause', 'gatherProcess') order by i.date")
    List<Instance> findGathering();

    @Query("select i from Instance i where i.state.id in (12, 9, 7, 13) and i.gatherMethodName = ?1")
    List<Instance> findIncomplete(String gatherMethodName);

    @Query("select i.state.name from Instance i where i.id = ?1")
    String getStateName(long instanceId);

    @Modifying
    @Query("update Instance i set i.state = (select s from State s where s.name = ?2) where i.id = ?1")
    void updateState(Long id, String stateName);

    @Query("select i from Instance i where not exists (select it from InstanceThumbnail it where it.id = i.id) " +
            "and i.state.name = 'archived'")
    List<Instance> findWithoutThumbnails(Pageable pageable);

    List<Instance> findByTitle(Title title);

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

    @Query("select i from Instance i\n" +
            "where i.state.name = 'gathered'\n" +
            "and i.title.awaitingConfirmation = false\n" +
            "and (:agencyId is null or i.title.agency.id = :agencyId)\n" +
            "and (:ownerId is null or i.title.owner.id = :ownerId)\n" +
            "order by i.date desc")
    Page<Instance> worktrayGathered(@Param("agencyId") Long agencyId, @Param("ownerId") Long ownerId, Pageable pageable);
}
